package nanukko.nanukko_back.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.User;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Product {
    ////////////////자동으로 추가 처리하는 필드//////////////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User seller; //판매자 id

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //상품 등록 날짜

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //상품 수정 날짜

    @Enumerated(EnumType.STRING)
    @NotNull
    @ColumnDefault("SELLING")
    private ProductStatus status; //상품 상태 -> Selling 디폴트

    @NotNull
    @Column(name = "is_deleted")
    private boolean isDeleted = false; //삭제 여부 -> true = 삭제, false = 삭제X -> false 디폴트


    //////////////기본 정보 필드//////////////
    @NotNull
    @Column(name = "product_name")
    private String productName; //상품 이름(제목)

    @NotNull
    private int price; //상품 가격

    @NotNull
    private String content; //상품 설명

    @Embedded
    private Image images; //상품 이미지

    @NotNull
    @Column(name = "thumbnail_image")
    private String thumbnailImage; //썸네일 이미지

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "product_condition")
    private Condition condition; //사용감


    //////////////// 카테고리 정보//////////////
    @ManyToOne
    @JoinColumn(name = "middle_id")
    @NotNull
    private MiddleCategory middleCategory;


    //////////////// 배송 관련 정보//////////////
    @Column(name = "is_person")
    @NotNull
    private boolean isPerson; //직거래 여부 -> true면 직거래

    @Column(name = "is_shipping")
    @NotNull
    private boolean isShipping; //배송 여부 -> true면 배송

    @Column(name = "free_shipping")
    @NotNull
    private boolean freeShipping; //배송비 포함 여부 -> true면 판매상품 가격에 배송비 포함, false면 포함X

    @Column(name = "shipping_fee")
    @NotNull
    @ColumnDefault("0")
    private int shippingFee = 0; //배송비


    ////////////////거래 관련 정보 //////////////
    @Column(name = "is_companion")
    private boolean isCompanion; //동행인 여부 -> true면 동행인 동행, false면 동행X

    @Column(name = "is_deputy")
    private boolean isDeputy; //대리인 여부 -> true면 대리인 사용, false면 사용X

    @Column(name = "companion_gender")
    private boolean gender; //동행인/대리인 성별 -> true면 남자, false면 여자

    @Column(name = "age_group")
    private String ageGroup; //동행인/대리인 나이


    ////////////////거래 장소 정보 //////////////
    @Column(name = "zipcode")
    private String zipCode;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;


    ////////////////그 외 정보 //////////////
    @ColumnDefault("0")
    @Column(name = "view_count")
    private int viewCount = 0; //조회수 default 0

    @ColumnDefault("0")
    @Column(name = "favorite_count")
    private int favoriteCount = 0; //찜 수 default 0

    @ColumnDefault("0")
    @Column(name = "talk_count")
    private int talkCount = 0; //채팅수 default 0


    ///////////////////////////////////////////////////////////////////
    //상품 상태 변경
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    // 카테고리 수정 메서드
    public void updateCategory(MiddleCategory middleCategory) {
        this.middleCategory = middleCategory;
    }

    //상품 수정
/*
    public void updateProduct(
            String productName,
            int price,
            ProductStatus status,
            String content,
            Condition condition,
            Image images,
            String thumbnailImage,
            boolean isPerson,
            boolean isDeputy,
            boolean isCompanion,
            boolean freeShipping,
            int shippingFee,
            LocalDateTime updatedAt
    ) {
        this.productName = productName;
        this.price = price;
        this.status = status;
        this.content = content;
        this.condition = condition;
        this.images = images;
        this.thumbnailImage = thumbnailImage;
        this.isPerson = isPerson;
        this.isDeputy = isDeputy;
        this.isCompanion = isCompanion;
        this.freeShipping = freeShipping;
        this.updatedAt = updatedAt;
    }
*/

    //상품 삭제
    public void removeProduct(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    //배송비 수정
    public void updateShippingFee(int shippingFree) {
        this.shippingFee = shippingFree;
    }

}
