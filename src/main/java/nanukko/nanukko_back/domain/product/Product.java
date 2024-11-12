package nanukko.nanukko_back.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User seller; //판매자 id

    @ManyToOne
    @JoinColumn(name = "middle_id")
    @NotNull
    private MiddleCategory middleCategory;

    @NotNull
    @Column(name = "product_name")
    private String productName; //상품 이름(제목)

    @Enumerated(EnumType.STRING)
    @NotNull
    private ProductStatus status; //상품 상태

    @NotNull
    private int price; //상품 가격

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //상품 등록 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //상품 수정 날짜
    @Column(name = "refreshed_at")
    private LocalDateTime refreshedAt; //끌올 날짜

    // private boolean hide; //숨김 여부 -> true = 숨김, false = 공개

    @NotNull
    @Column(name = "view_count")
    private int viewCount; //조회수

    @NotNull
    @Column(name = "is_deleted")
    private boolean isDeleted; //삭제 여부 -> true = 삭제, false = 삭제X

    @Embedded
    private Image images; //상품 이미지

    @NotNull
    @Column(name = "thumbnail_image")
    private String thumbnailImage; //썸네일 이미지

    @NotNull
    private String content; //상품 설명

    @Embedded
    @NotNull
    private Condition condition; //사용감

    @Column(name = "is_person")
    @NotNull
    private boolean isPerson; //직거래 여부 -> true면 직거래, false면 배송거래

    @Column(name = "is_deputy")
    private boolean isDeputy; //대리인 여부 -> true면 대리인 사용, false면 사용X

    @Column(name = "is_companion")
    private boolean isCompanion; //동행인 여부 -> true면 동행인 동행, false면 동행X

    @Column(name = "free_shipping")
    @NotNull
    private boolean freeShipping; //배송비 포함 여부 -> true면 판매상품 가격에 배송비 포함, false면 포함X

    @Column(name = "shipping_free")
    private int shippingFree; //배송비



    //상품 상태 변경
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

}
