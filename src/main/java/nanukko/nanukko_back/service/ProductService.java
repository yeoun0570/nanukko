package nanukko.nanukko_back.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Condition;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.file.FileDTO;
import nanukko.nanukko_back.dto.file.FileDirectoryType;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import nanukko.nanukko_back.dto.user.UserProductDTO;
import nanukko.nanukko_back.repository.MiddleCategoryRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.repository.WishlistRepository;
import nanukko.nanukko_back.util.ProductMapper;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ModelMapper modelMapper;
    private final MiddleCategoryRepository middleCategoryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final ImageService imageService;

    public Product createProduct(ProductRequestDto dto, List<MultipartFile> images, String userId) {
        // 현재 로그인한 사용자 조회
        User seller = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자 찾기 실패"));

        // 중분류 카테고리 조회
        MiddleCategory middleCategory = middleCategoryRepository.findById(dto.getMiddleId()).orElseThrow(() -> new IllegalArgumentException("카테고리 찾기 실패"));

        //이미지 업로드, URL 리스트
        List<FileDTO> imgUrls = imageService.uploadMultipleFiles(images, FileDirectoryType.SELL, seller.getUserId());
//        List<String> imgUrls = fileService.uploadProductImages(images, "products", 500);

        log.info("업로드 이미지 url : {}" , imgUrls);
        Image image = new Image(imgUrls);

        //상품 엔티티 생성
        Product product = Product.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seller(seller)
                .productName(dto.getProductName())
                .price(dto.getPrice())
                .content(dto.getContent())
                .images(image)
                .thumbnailImage(image.getImage1())
                .condition(Condition.valueOf(dto.getCondition()))
                .middleCategory(middleCategory)
                .isPerson(dto.getIsPerson())
                .isShipping(dto.getIsShipping())
                .freeShipping(dto.getFreeShipping())
                .shippingFee(dto.getShippingFee())
                .isCompanion(dto.getIsCompanion())
                .isDeputy(dto.getIsDeputy())
                .gender(dto.getGender())
                .ageGroup(dto.getAgeGroup())
                .zipCode(dto.getZipCode())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        return productRepository.save(product);
    }

    public ProductRequestDto getProductDtoById (Long productId) { //이건 나중에 지워도 됨
        Product product = getProductById(productId);
        return modelMapper.map(product, ProductRequestDto.class);
    }

    public ProductResponseDto getProductDetail (Long productId, User user) {
        Product product = getProductById(productId);
        boolean isWished = user != null && wishlistRepository.existsByUserAndProduct(user, product);
        ProductResponseDto dto = ProductMapper.toDto(product);
        dto.setIsWished(isWished);
        return dto;
    }

    public PageResponseDTO<Product> searchProducts(String query, Pageable pageable) { //상품명 검색 페이지별 조회
        Page<Product> searchResult  = productRepository.searchByName(query, pageable);
        return new PageResponseDTO<>(searchResult);
    }

    public PageResponseDTO<Product> findByMajorCategory(Long majorId, Pageable pageable) { //대분류별 상품 조회
        Page<Product> result = productRepository.findByMajorId(majorId, pageable);
        return new PageResponseDTO<>(result);
    }

    public PageResponseDTO<Product> findByMiddleCategory(Long middleId, Pageable pageable) { //중분류별 상품 조회
        Page<Product> result = productRepository.findByMiddleId(middleId, pageable);
        return new PageResponseDTO<>(result);
    }

    public List<ProductResponseDto> findRelatedProducts(Long productId) {
        // 1. 현재 상품의 중분류 ID 조회
        Product currentProduct = getProductById(productId);
        Long middleId = currentProduct.getMiddleCategory().getMiddleId();
        log.info("연관상품 조회 중..." + middleId);
        // 2. 동일한 중분류의 다른 상품들 조회
        List<Product> products = productRepository.findRelatedProducts(productId, middleId);
        log.info("연관상품 조회 중...List<Product> 몇 개? : " + products.size());

        List<ProductResponseDto> dto = products.stream()
                .map(ProductMapper::toDto) // 각 Product를 ProductResponseDto로 변환
                .toList();
        log.info("연관 상품 Dto : " + dto);
        return dto;
    }

    ///////////////////공통 메서드//////////////////////
    private Product getProductById (Long productId) { //공통 메서드
        return productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없음"));
    }

}