package nanukko.nanukko_back.repository.chat;

import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.ChatRoom;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {

    //채팅 목록 들고오기
    Page<ChatMessages> findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    //사용자 마지막 퇴장 시점 이후의 메시지 목록만 출력
    @Query(
            "SELECT m FROM ChatMessages m " +
                    "WHERE m.chatRoom.chatRoomId = :chatRoomId " +
                    "AND (" +
                    "(:userId = m.chatRoom.product.seller.userId AND (m.createdAt > m.chatRoom.sellerLeftAt OR m.chatRoom.sellerLeftAt IS NULL)) " +
                    "OR " +
                    "(:userId = m.chatRoom.buyer.userId AND (m.createdAt > m.chatRoom.buyerLeftAt OR m.chatRoom.buyerLeftAt IS NULL))" +
                    ") " +
                    "ORDER BY m.createdAt DESC"
    )
    Page<ChatMessages> findMessagesSinceLastExit(@Param("chatRoomId")Long chatRoomId, @Param("userId") String userId, Pageable pageable);

    // 채팅방의 안 읽은 메시지 수 카운트
    long countByChatRoom_ChatRoomIdAndIsReadFalseAndSender_UserIdNot(
            Long chatRoomId,
            String userId
    );

    // 채팅방의 마지막 메시지 조회
    Optional<ChatMessages> findFirstByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 안 읽은 메시지 목록 조회
    List<ChatMessages> findByChatRoom_ChatRoomIdAndIsReadFalseAndSender_UserIdNot(
            Long chatRoomId,
            String userId
    );


}

