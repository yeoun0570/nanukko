package nanukko.nanukko_back.domain.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
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
    private ProductStatus status = ProductStatus.SELLING; //상품 상태 or @ColumnDefault("'SELLING'")

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; //삭제 여부 -> true = 삭제, false = 삭제X -> false 디폴트


    //////////////기본 정보 필드//////////////
    @NotNull
    @Column(name = "product_name")
    private String productName; //상품 이름(제목)

    @NotNull
    private int price; //상품 가격

    @NotNull
    @Column(columnDefinition = "TEXT")
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

    @Column(name = "has_delivery")
    @NotNull
    private boolean hasDelivery = false; //배송 여부 -> true면 배송시작, false면 배송 안했음

    ////////////////거래 관련 정보 //////////////
    @Column(name = "is_companion")
    private Boolean isCompanion; //동행인 여부 -> true면 동행인 동행, false면 동행X

    @Column(name = "is_deputy")
    private Boolean isDeputy; //대리인 여부 -> true면 대리인 사용, false면 사용X

    @Column(name = "companion_gender")
    private Boolean gender; //동행인/대리인 성별 -> true면 남자, false면 여자

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
    @PrePersist
    private void prePersist() {
        if (this.status == null) {
            this.status = ProductStatus.SELLING; // 기본값 설정
        }
        this.isDeleted = this.isDeleted != true ? false : this.isDeleted;
        if (this.viewCount == 0) this.viewCount = 0;
        if (this.favoriteCount == 0) this.favoriteCount = 0;
        if (this.talkCount == 0) this.talkCount = 0;
    }


    //상품 상태 변경
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    // 카테고리 수정 메서드
    public void updateCategory(MiddleCategory middleCategory) {
        this.middleCategory = middleCategory;
    }

    // 배송여부 변경
    public void updateHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    //상품 삭제
    public void removeProduct(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    //배송비 수정
    public void updateShippingFee(int shippingFree) {
        this.shippingFee = shippingFree;
    }

    //상품 수정
    public void updateProduct(ProductRequestDto dto) {
        this.updatedAt = LocalDateTime.now();
        this.productName = dto.getProductName();
        this.price = dto.getPrice();
        this.content = dto.getContent();
        this.condition = Condition.valueOf(dto.getCondition());
        this.isPerson = dto.getIsPerson();
        this.isShipping = dto.getIsShipping();
        this.freeShipping = dto.getFreeShipping();
        this.shippingFee = dto.getShippingFee();
        this.isCompanion = dto.getIsCompanion();
        this.isDeputy = dto.getIsDeputy();
        this.gender = dto.getGender();
        this.ageGroup = dto.getAgeGroup();
        this.zipCode = dto.getZipCode();
        this.address = dto.getAddress();
        this.detailAddress = dto.getDetailAddress();
        this.latitude = dto.getLatitude();
        this.longitude = dto.getLongitude();
    }

}
