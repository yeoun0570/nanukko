package nanukko.nanukko_back.dto.user;

import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.Condition;
import nanukko.nanukko_back.domain.product.Image;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//상품 등록(판매하기), 상품 수정에 사용할 DTO
public class UserSetProductDTO {
    private boolean userGender;
    private Long productId;
    private String productName;
    private Image images;
    private String thumbnailImage;
    private int price;
    private Long majorId; //메이저 카테고리 ID
    private String majorName;
    private Long middleId; //중분류 카테고리 ID
    private String middleName;
    private Long descriptionId; //상품 설명 ID
    private String color; // 상품 색
    private String content; //상품 설명
    private Condition condition; //사용감
    //직거래 가능 장소는 API 적용 후에 다시 작성
}
