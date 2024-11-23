package nanukko.nanukko_back.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.Condition;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDto {
    // 기본 정보
    private String productName;
    private int price;
    private String content;
    private Condition condition; // Condition의 String 값

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

}
