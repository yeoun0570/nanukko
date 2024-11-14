package nanukko.nanukko_back.domain.report;

import jakarta.persistence.*;
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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId; //신고 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User reporter; //신고자 ID (FK)

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; //상품 ID (FK)

    @NotNull
    private ReportType type; //신고 타입

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성 날짜

    @NotNull
    private boolean status; //신고 처리 상태 -> true = 처리완료, false = 미처리
}
