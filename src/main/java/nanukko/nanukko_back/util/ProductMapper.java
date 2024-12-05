package nanukko.nanukko_back.util;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.dto.product.ProductResponseDto;

public class ProductMapper {
    // Product 엔티티를 ProductResponseDto로 매핑
    public static ProductResponseDto toDto(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .content(product.getContent())
                .condition(product.getCondition().toString())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .status(product.getStatus().toString())
                .userId(product.getSeller().getUserId())
                .reviewRate(product.getSeller().getReviewRate())
                .profile(product.getSeller().getProfile())
                .thumbnailImage(product.getThumbnailImage())
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

    public static ProductResponseDto toDtoSimple(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .status(product.getStatus().toString())
                .userId(product.getSeller().getUserId())
                .reviewRate(product.getSeller().getReviewRate())
                .profile(product.getSeller().getProfile())
                .thumbnailImage(product.getThumbnailImage())
                .image1(product.getImages().getImage1())
                .address(product.getAddress())
                .build();
    }


}
