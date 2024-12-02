package nanukko.nanukko_back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import nanukko.nanukko_back.dto.review.ReviewInMyStoreDTO;
import nanukko.nanukko_back.dto.user.*;
import nanukko.nanukko_back.exception.ErrorResponse;
import nanukko.nanukko_back.service.ImageService;
import nanukko.nanukko_back.service.UserService;
import nanukko.nanukko_back.util.ProductMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/my-store")
public class UserController {
    private final UserService userService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    //사용자가 자신의 정보 조회
    @GetMapping("/info")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails  // 현재 로그인한 사용자(시큐리티)
    ) {
        String userId = userDetails.getUsername();
        log.info("userId: {}", userId);
        UserInfoDTO userInfoDTO = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfoDTO);
    }

    //사용자가 자신의 정보 수정
    @PostMapping("/modify")
    public ResponseEntity<UserInfoDTO> modifyUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserInfoDTO userInfoDTO
    ) {
        try {
            String userId = userDetails.getUsername();
            if (userInfoDTO.getProfile() != null) {
                imageService.deleteOldProfile(userId); //이전 이미지 NCP Object Storage에서 삭제
            }
            userInfoDTO.setUserId(userId);
            log.info("Received data: {}", userInfoDTO);  // 로깅 추가
            UserInfoDTO modifiedUser = userService.modifyUserInfo(userInfoDTO);
            return ResponseEntity.ok(modifiedUser);
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패", e);  // 에러 로깅
            return ResponseEntity.badRequest().build();
        }
    }

    //사용자가 판매중, 판매완료 상품 조회
    @GetMapping("/sale-products")
    public ResponseEntity<PageResponseDTO<UserProductDTO>> getSellProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,  // 현재 로그인한 사용자(시큐리티)
            @RequestParam(required = false, defaultValue = "SELLING") ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String userId = userDetails.getUsername();
        log.info("Authenticated user: {}, page: {}, size: {}", userDetails.getUsername(), page, size);  // 인증된 사용자 정보 로깅
        Pageable pageable = PageRequest.of(page, size);

        PageResponseDTO<UserProductDTO> products = userService.getSellProducts(userId, status, pageable);
        return ResponseEntity.ok(products);
    }

    //사용자가 판매중인 상품 수정 api/my-store/sale-products/modify
    @PostMapping(value = "/sale-products/modify" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> modifyProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("productInfo") String productInfoJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {
        log.info("=== 상품 수정 요청 시작 ===");
        log.info("이미지 개수: {}", images.size());

        String userId = userDetails.getUsername();

        log.info("=== JSON 문자열을 DTO로 변환 시작 ===");
        ProductRequestDto requestDto = objectMapper.readValue(productInfoJson, ProductRequestDto.class);

        log.info("=== 상품 수정 시작 ===");
        Product modified = userService.modifyProduct(userId, requestDto, images);

        log.info("=== Product -> ProductResponseDto 변환 ===");
        ProductResponseDto result = ProductMapper.toDto(modified);
        log.info("리턴 값 : {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sale-products/remove")
    public ResponseEntity<?> removeProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        try {
            String userId = userDetails.getUsername();
            UserRemoveProductDTO response = userService.removeProduct(userId, productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }


    //사용자가 구매중, 구매완료 상품 조회
    @GetMapping("/buy-products")
    public ResponseEntity<PageResponseDTO<UserOrderDTO>> getOrderProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "ESCROW_HOLDING") PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String userId = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size);

        PageResponseDTO<UserOrderDTO> products = userService.getOrderProducts(userId, status, pageable);
        return ResponseEntity.ok(products);
    }

    //찜 목록 조회
    @GetMapping("/wishlist")
    public ResponseEntity<PageResponseDTO<UserWishlistDTO>> getWishlists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String userId = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size);

        PageResponseDTO<UserWishlistDTO> wishlists = userService.getWishlists(userId, pageable);
        return ResponseEntity.ok(wishlists);
    }

    //찜 목록에서 제거
    @PostMapping("/wishlist/remove")
    public ResponseEntity<Void> removeWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        String userId = userDetails.getUsername();
        userService.removeWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    //후기 조회
    @GetMapping("/reviews")
    public ResponseEntity<PageResponseDTO<ReviewInMyStoreDTO>> getReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    ) {
        String userId = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size);

        PageResponseDTO<ReviewInMyStoreDTO> reviews = userService.getReview(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    //탈퇴하기
    @PostMapping("/remove")
    public ResponseEntity<?> removeUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            String userId = userDetails.getUsername();
            UserRemoveDTO result = userService.removeUser(userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    //사이드 바에 넣을 사용자 프로필 정보
    @GetMapping("/simple-info")
    public ResponseEntity<UserSimpleInfoDTO> getSimpleInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        UserSimpleInfoDTO response = userService.getSimpleInfo(userId);

        return ResponseEntity.ok(response);
    }
}
