package nanukko.nanukko_back.domain.wishlist;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "wish_id")
    private Long wishId; //관심상품 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user; //사용자 ID (FK)

    @ManyToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product; //상품 ID (FK)

}
