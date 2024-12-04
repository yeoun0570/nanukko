package nanukko.nanukko_back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/products")
public class ProductController {
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @GetMapping("/main")
    public ResponseEntity<PageResponseDTO<ProductResponseDto>> getMain(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponseDTO<ProductResponseDto> dto;
        //로그인 : 사용자 자녀 정보 기반 상품 추천 (찜 여부 포함)
        dto = productService.getMainProducts(pageable, userDetails.getUsername());
        return ResponseEntity.ok(dto);

    }

    @GetMapping("/main/logout")
    public ResponseEntity<PageResponseDTO<ProductResponseDto>> getMainLogout(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponseDTO<ProductResponseDto> dto; //로그아웃 : 전체 상품 조회
        dto = productService.getMainProducts(pageable);
        return ResponseEntity.ok(dto);
    }


    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map> createProduct(
            @RequestPart("productInfo") String productInfoJson,  // JSON 문자열로 받음
            @RequestPart(value = "images", required = false) List<MultipartFile> images,  // 파일 데이터
            @AuthenticationPrincipal CustomUserDetails userDetails) throws JsonProcessingException {

        log.info("=== 상품 등록 요청 시작 ===");
        log.info("productInfoJson: {}", productInfoJson);
        log.info("이미지 개수: {}", images.size());

        String userId = userDetails.getUsername();
        log.info("인증된 사용자 ID: {}", userId);

        log.info("=== JSON 문자열을 DTO로 변환 시작 ===");
        ProductRequestDto productRequestDto = objectMapper.readValue(productInfoJson, ProductRequestDto.class);

        Product newProduct = productService.createProduct(productRequestDto, images, userId);
        log.info("생성된 상품: {}", newProduct);

        Map<String, Object> response = Map.of(
                "productId", newProduct.getProductId(),
                "productName", newProduct.getProductName()
        );

        log.info("응답 데이터: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            String userId = userDetails.getUsername();
            currentUser = userRepository.findById(userId).orElse(null);
        }
        ProductResponseDto product = productService.getProductDetail(id, currentUser);
        log.info("ProductResponseDto 전달 : {}", product);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/related")
    public ResponseEntity<List<ProductResponseDto>> getRelatedProducts(@RequestParam Long productId) {
        List<ProductResponseDto> relatedProducts = productService.findRelatedProducts(productId);
        log.info("연관 상품 전달 ?" + relatedProducts.stream().toList());
        return ResponseEntity.ok(relatedProducts);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<Product>> searchProducts(
            @RequestParam(required = false, defaultValue = "") @Size(min = 1, max = 100) String query,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 검색어가 비어있는 경우 빈 결과 반환
        if (query.trim().isEmpty()) {
            return ResponseEntity.ok(PageResponseDTO.empty(pageable));
        }

        User currentUser = null;
        if (userDetails != null) {
            String userId = userDetails.getUsername();
            currentUser = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없음"));
        }

        try {
            PageResponseDTO<Product> products = productService.searchProducts(query.trim(), pageable, currentUser);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 검색 요청입니다: " + e.getMessage());
        }
    }


    //카테고리별 조회 : 대분류 전체 상품 조회
    @GetMapping("/category/major")
    public ResponseEntity<PageResponseDTO<Product>> getProductsByCategory(
            @RequestParam Long majorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        PageResponseDTO<Product> products = productService.findByMajorCategory(majorId, pageRequest);

        return ResponseEntity.ok(products);
    }

    //카테고리별 조회 : 중분류 전체 상품 조회
    @GetMapping("/category/middle")
    public ResponseEntity<PageResponseDTO<Product>> getProductsByMiddleCategory(
            @RequestParam(name = "middleId") Long value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        PageResponseDTO<Product> products = productService.findByMiddleCategory(value, pageRequest);

        return ResponseEntity.ok(products);
    }


}
