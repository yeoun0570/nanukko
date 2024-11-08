package nanukko.nanukko_back.domain.chat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
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

    @Column(name = "is_seller_leaved")
    @NotNull
    private boolean isSellerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감
    @Column(name = "is_buyer_leaved")
    @NotNull
    private boolean isBuyerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감
}
