package nanukko.nanukko_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.Condition;
import nanukko.nanukko_back.domain.product.Image;
import nanukko.nanukko_back.domain.product.ProductStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//상품 등록(판매하기), 상품 수정에 사용할 DTO
public class UserSetProductDTO {
    private Long productId;
    private String productName;
    private Image images;
    private String thumbnailImage;
    private int price;
    private Long majorId; //메이저 카테고리 ID
    private String majorName;
    private Long middleId; //중분류 카테고리 ID
    private String middleName;
    private ProductStatus status;
    private String content; //상품 설명
    private Condition condition; //사용감
    private boolean isPerson; //직거래 여부 -> true면 직거래, false면 배송거래
    private boolean isDeputy; //대리인 여부 -> true면 대리인 사용, false면 사용X
    private boolean deputyGender;
    private boolean isCompanion; //동행인 여부 -> true면 동행인 동행, false면 동행X
    private boolean companionGender;
    private boolean freeShipping; //배송비 포함 여부 -> true면 판매상품 가격에 배송비 포함, false면 포함X
    private int shippingFree; //배송비



    //직거래 가능 장소는 API 적용 후에 다시 작성
}
