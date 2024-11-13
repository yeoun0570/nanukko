package nanukko.nanukko_back.domain.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId; //후기 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user; // 후기 작성한 사용자 ID (FK)

    @OneToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product; // 후기가 작성될 상품 (FK)

    @NotNull
    private String review; // 후기 메시지

    @NotNull
    @Min(0)
    @Max(100)
    private int rate; //평점

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정 날짜
}
