package nanukko.nanukko_back.domain.chat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"chatMessages", "product", "buyer"})//순환 참조를 없애는 설정
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id  // @Id 만으로도 not null 제약조건 포함
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId; //채팅방 ID

    @ManyToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product; // 상품 ID (FK)

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User buyer; // 구매자 ID (FK)

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<ChatMessages> chatMessages; //채팅메시지 ID (FK)

    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성 날짜

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정 날짜

    @Column(name = "seller_left_at")
    private LocalDateTime sellerLeftAt=null;

    @Column(name = "buyer_left_at")
    private LocalDateTime buyerLeftAt=null;

    // 판매자/구매자 여부 확인
    public boolean isSeller(String userId) {
        return this.product.getSeller().getUserId().equals(userId);
    }

    public boolean isBuyer(String userId) {
        return this.buyer.getUserId().equals(userId);
    }

    // 채팅방 멤버인지 확인
    public boolean isMember(String userId) {
        return isSeller(userId) || isBuyer(userId);
    }

    // 나가기 시간 기록
    public void updateLeftAt(String userId) {
        if (isSeller(userId)) {
            this.sellerLeftAt = LocalDateTime.now();
        } else if (isBuyer(userId)) {
            this.buyerLeftAt = LocalDateTime.now();
        }
    }

    // 재입장 처리 (나가기 시간 null로 설정)
    public void clearLeftAt(String userId) {
        if (isSeller(userId)) {
            this.sellerLeftAt = null;
        } else if (isBuyer(userId)) {
            this.buyerLeftAt = null;
        }
    }

    // 이미 나간 상태인지 확인
    public boolean isAlreadyLeft(String userId) {
        if (isSeller(userId)) {
            return this.sellerLeftAt != null;
        } else if (isBuyer(userId)) {
            return this.buyerLeftAt != null;
        }
        return false;
    }

    @Builder
    public ChatRoom(Product product, User buyer) {
        this.product = product;
        this.buyer = buyer;
//        this.isSellerLeaved = false;
//        this.isBuyerLeaved = false;
    }

    // 정적 팩토리 메서드 추가
    public static ChatRoom createChatRoom(Product product, User buyer) {
        return ChatRoom.builder()
                .product(product)
                .buyer(buyer)
                .build();
    }
}
