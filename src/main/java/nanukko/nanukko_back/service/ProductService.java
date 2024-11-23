package nanukko.nanukko_back.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.repository.MiddleCategoryRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final MiddleCategoryRepository middleCategoryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ProductRepository productRepository;

    public void createProduct(ProductRequestDto dto, List<MultipartFile> images) {
        // 현재 로그인한 사용자 조회
        User seller = userRepository.findById("123").orElseThrow(() -> new IllegalArgumentException("사용자 찾기 실패"));

        // 중분류 카테고리 조회
        MiddleCategory middleCategory = middleCategoryRepository.findById(dto.getMiddleId()).orElseThrow(() -> new IllegalArgumentException("카테고리 찾기 실패"));

        //이미지 업로드, URL 리스트
        List<String> imgUrls = fileService.uploadProductImages(images, "products", 500);
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
                .condition(dto.getCondition())
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

        productRepository.save(product);
    }


}
