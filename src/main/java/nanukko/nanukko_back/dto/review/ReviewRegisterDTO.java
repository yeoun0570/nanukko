package nanukko.nanukko_back.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewRegisterDTO {
    private String orderId;
    private String authorId; // 후기 작성자
    private String review;
    @Min(0)
    @Max(100)
    private int rate; //후기 평점
}
