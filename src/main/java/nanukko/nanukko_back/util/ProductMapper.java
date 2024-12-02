package nanukko.nanukko_back.util;

import nanukko.nanukko_back.domain.product.Condition;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public class ProductMapper {
    // Product 엔티티를 ProductResponseDto로 매핑
    public static ProductResponseDto toDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .content(product.getContent())
                .condition(product.getCondition().toString())
                .userId(product.getSeller().getUserId())
                .reviewRate(product.getSeller().getReviewRate())
                .profile(product.getSeller().getProfile())
                .image1(product.getImages().getImage1())
                .image2(product.getImages().getImage2())
                .image3(product.getImages().getImage3())
                .image4(product.getImages().getImage4())
                .image5(product.getImages().getImage5())
                .majorId(product.getMiddleCategory().getMajor().getMajorId())
                .middleId(product.getMiddleCategory().getMiddleId())
                .isPerson(product.isPerson())
                .isShipping(product.isShipping())
                .freeShipping(product.isFreeShipping())
                .shippingFee(product.getShippingFee())
                .isCompanion(product.getIsCompanion())
                .isDeputy(product.getIsDeputy())
                .gender(product.getGender())
                .ageGroup(product.getAgeGroup())
                .address(product.getAddress())
                .detailAddress(product.getDetailAddress())
                .latitude(product.getLatitude())
                .longitude(product.getLongitude())
                .favorite_count(product.getFavoriteCount())
                .view_count(product.getViewCount())
                .talk_count(product.getTalkCount())
                .build();
    }
}
