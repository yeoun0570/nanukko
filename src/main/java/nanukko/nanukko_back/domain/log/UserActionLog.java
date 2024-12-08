package nanukko.nanukko_back.domain.log;

import jakarta.persistence.*;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_action_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType; //SEARCH, CLICK, PURCHASE

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int ageGroup;
}
