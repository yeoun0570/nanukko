package nanukko.nanukko_back.repository.chat;

import nanukko.nanukko_back.domain.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    //채팅방 목록 조회
//    @Query("SELECT c FROM ChatRoom c WHERE c.buyer = :userId OR c.product.seller = :userId")
//    Page<ChatRoom> findChatRoomsByUserId(@Param("userId") String userId, Pageable pageable);
//
//    Page<ChatRoom> findByBuyer_UserIdOrProduct_Seller_UserId(String userId, String sameUserId, Pageable pageable);

    // 나가지 않은 채팅방만 수정 일자 기준 asc 조회
    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    // 연관 엔티티들을 한 번에 조회
    Page<ChatRoom> findByBuyer_UserIdAndIsBuyerLeavedFalseOrProduct_Seller_UserIdAndIsSellerLeavedFalseOrderByUpdatedAt(
            String buyerId,
            String sellerId,
            Pageable pageable
    );


    // 단일 ChatRoom 타입으로 반환
    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    @Query("SELECT c FROM ChatRoom c " +
            "WHERE (c.buyer.userId = :userId) " +
            //" AND c.isBuyerLeaved = false) " +
            "OR (c.product.seller.userId = :userId) " +
            //"AND c.isSellerLeaved = false) " +
            "ORDER BY c.updatedAt")
    ChatRoom findOneChatRoom(
            @Param("userId") String userId
    );

    Optional<ChatRoom> findByBuyer_UserIdAndProduct_ProductId(String buyerUserId, Long productId);
}