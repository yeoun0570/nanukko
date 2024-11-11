package nanukko.nanukko_back.domain.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
    private User authorId; // 후기 작성한 사용자 ID (FK)

    @NotNull
    private String review; // 후기 메시지

    @NotNull
    private int score; //평점

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정 날짜
}
