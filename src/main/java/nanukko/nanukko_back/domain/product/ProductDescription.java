package nanukko.nanukko_back.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class ProductDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "description_id")
    private Long descriptionId; // 상품 설명 ID

    @OneToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product; //상품 ID (FK)

    @NotNull
    private String content; //상품 설명

    @Embedded
    @NotNull
    private Condition condition; //사용감
}
