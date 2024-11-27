package nanukko.nanukko_back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/new")
    public ResponseEntity<Map> createProduct(
            @RequestPart(value = "productInfo") @Valid ProductRequestDto productRequestDto,
            @RequestPart(value = "images") List<MultipartFile> images)
    //@AuthenticationPrincipal UserDetails userDetails 로그인 유저 정보 받아오기*** 서비스 로직 수정 필***
    {
        try {
            Product newProduct = productService.createProduct(productRequestDto, images);

            //알림, 이메일 전송 등 추가 로직 구현 가능

            return ResponseEntity.ok(
                    Map.of(
                            "productId", newProduct.getProductId(),
                            "productName", newProduct.getProductName()
                    )
            );
        } catch (Exception e) {
            log.error("상품 등록 에러: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRequestDto> getProductById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            String userId = userDetails.getUsername();
            currentUser = userRepository.findById(userId);
        }
        ProductRequestDto product = productService.getProductDetail(id, currentUser);
        return ResponseEntity.ok(product);
    }


    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<Product>> searchProducts(
            @RequestParam(required = false, defaultValue = "") @Size(min = 1, max = 100) String query,
            @PageableDefault(size = 20, sort = "updatedTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 검색어가 비어있는 경우 빈 결과 반환
        if (query.trim().isEmpty()) {
            return ResponseEntity.ok(PageResponseDTO.empty(pageable));
        }

        try {
            PageResponseDTO<Product> products = productService.searchProducts(query.trim(), pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 검색 요청입니다: " + e.getMessage());
        }
    }


    //카테고리별 조회 : 대분류
    @GetMapping("/major")
    public ResponseEntity<PageResponseDTO<Product>> getProductsByCategory(
            @RequestParam Long majorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedTime"));
        PageResponseDTO<Product> products = productService.findByMajorCategory(majorId, pageRequest);

        return ResponseEntity.ok(products);
    }

    //카테고리별 조회 : 중분류
    @GetMapping("/middle")
    public ResponseEntity<PageResponseDTO<Product>> getProductsByMiddleCategory(
            @RequestParam(name = "middleId") Long value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedTime"));
        PageResponseDTO<Product> products = productService.findByMiddleCategory(value, pageRequest);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/related")
    public ResponseEntity<List<Product>> getRelatedProducts(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Product> relatedProducts = productService.findRelatedProducts(productId, limit);
        return ResponseEntity.ok(relatedProducts);
    }

/*
    @PostMapping("/new")
    public ResponseEntity<?> createNewProduct (
            @RequestBody()


                                        @RequestParam("file") MultipartFile file, //******
                                        @RequestParam("reviewContents") String reviewContents,
                                        @RequestParam("reservationId") Long reservationId,
                                        @RequestParam("registerDate") String registerDate,
                                        @RequestParam("reviewScore") Long reviewScore,
                                        @RequestParam("userEmail") String userEmail) {
        try {
            // *******
            List<FileDTO> fileDTOList = fileService.uploadFiles(List.of(file), "reviews");
            String img = fileDTOList.get(0).getUploadFileUrl();

            // Save review details
            ReviewRequest reviewRequest = ReviewRequest.builder()
                    .reviewContents(reviewContents)
                    .reservationId(reservationId)
                    .registerDate(LocalDate.parse(registerDate))
                    .reviewScore(reviewScore)
                    .userEmail(userEmail)
                    .img(img)
                    .build();

            myDiningService.registerReview(reviewRequest);

            return ResponseEntity.ok("File uploaded and review saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }*/

    /*// 현재 이미지를 varchar(255)로 받아서 이미지 크기를 담을 수가 없어서 생성이 안됨. 나중에 클라우드 도입시키고 새로 짜야될거 같음
    @PostMapping("/product/register")
    public ResponseEntity<?> registerProduct(
            @RequestParam String userId,
            @RequestBody UserSetProductDTO productDTO
    ) {
        try {
            log.info("Received userId: {}", userId);
            log.info("Received productDTO: {}", productDTO);

            UserSetProductDTO savedProduct = userService.registerProduct(userId, productDTO);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("Error registering product", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }*/

}
