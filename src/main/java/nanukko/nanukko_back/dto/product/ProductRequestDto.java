package nanukko.nanukko_back.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.Condition;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDto {
    // 기본 정보
    private Long id;
    private String productName;
    private int price;
    private String content;
    private String condition; // Condition의 String 값

    // 이미지 정보
    private String image1;
    private String image2;
    private String image3;
    private String image4;
    private String image5;
    private String thumbnailImage;

    // 카테고리 정보
    private Long majorId; //
    private Long middleId; // 중분류 카테고리 id

    // 배송 관련 정보
    private Boolean isPerson;
    private Boolean isShipping;
    private Boolean freeShipping;
    private Integer shippingFee;

    // 거래 관련 정보
    private Boolean isCompanion;
    private Boolean isDeputy;
    private Boolean gender;
    private String ageGroup;

    // 거래 장소 정보
    private String zipCode;
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;

    // 찜 여부
    private Boolean isWished;

    //좋아요 수
    private Integer favorite_count;
    private Integer view_count;
    private Integer talk_count;

}
