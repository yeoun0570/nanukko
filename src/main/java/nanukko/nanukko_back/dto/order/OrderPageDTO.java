package nanukko.nanukko_back.dto.order;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
//주문(결제) 페이지에 사용될 데이터
public class OrderPageDTO {
    //상품정보
    private String thumbnailImage;
    private String productName;
    private int price;
    private double chargeAmount;
    private int totalAmount;
    //구매자 정보
    private String nickname;
    private String addrMain; 
    private String addrDetail;
    private String addrZipcode;
    private String mobile;
    //추가(요청사항) 현재는 보류
//    private String orderMessage;
}
