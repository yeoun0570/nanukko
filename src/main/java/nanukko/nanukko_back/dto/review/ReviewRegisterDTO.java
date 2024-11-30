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
public class ReviewRegisterDTO {
    private String orderId;
    private String review;
    @Min(0)
    @Max(10)
    private int rate; //후기 평점
}
