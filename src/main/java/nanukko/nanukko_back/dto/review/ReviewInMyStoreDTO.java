package nanukko.nanukko_back.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//내 상점에서 후기 조회하기 위한 DTO
public class ReviewInMyStoreDTO {
    private Long reviewId;
    private Long productId;
    private String productName;
    private String authorId;
    private String thumbnail;
    private String authorNickName;
    private String review;
    private int rate;
    @Min(0)
    @Max(100)
    private double reviewRate; //상점 평점
}
