package nanukko.nanukko_back.repository.chat;

import nanukko.nanukko_back.domain.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    //채팅방 목록 조회
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


        @Query(
                "SELECT DISTINCT c FROM ChatRoom c " +
                        "JOIN FETCH c.product p " +
                        "JOIN FETCH c.buyer b " +
                        "JOIN FETCH p.seller s " +
                        "WHERE " +
                        "(c.buyer.userId = :userId AND c.isBuyerLeft = false) OR " +
                        "(p.seller.userId = :userId AND c.isSellerLeft = false) " +
                        "ORDER BY c.updatedAt DESC"
        )
        Page<ChatRoom> findActiveRoomsByUserId(
                @Param("userId") String userId,
                Pageable pageable
        );


    @Query("SELECT c FROM ChatRoom c " +
            "WHERE c.product.productId = :productId " +
            "AND ((c.buyer.userId = :userId AND c.isBuyerLeft = false) " +
            "OR (c.product.seller.userId = :userId AND c.isSellerLeft = false)) " +
            "ORDER BY c.updatedAt DESC")
    Optional<ChatRoom> findActiveRoomByProductAndUser(
            @Param("productId") Long productId,
            @Param("userId") String userId
    );

    // 채팅방 목록 조회시 사용할 쿼리
    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    Page<ChatRoom> findByBuyer_UserIdAndIsBuyerLeftFalseOrProduct_Seller_UserIdAndIsSellerLeftFalseOrderByUpdatedAtDesc(
            String buyerId,
            String sellerId,
            Pageable pageable
    );



    // 특정 상품에 대한 채팅방 조회
    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    Optional<ChatRoom> findByBuyer_UserIdAndProduct_ProductId(
            String buyerUserId,
            Long productId
    );

    //특정 상품에 대해 판매자가 나간 채팅방 찾기
    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    Optional<ChatRoom> findByProduct_ProductIdAndProduct_Seller_UserIdAndSellerLeftAtIsNotNull(
            Long productId,
            String sellerId
    );


    @EntityGraph(attributePaths = {"product", "product.seller", "buyer"})
    Optional<ChatRoom> findByProduct_ProductIdAndProduct_Seller_UserId(
            Long productId,
            String sellerId
    );
}