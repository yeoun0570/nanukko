package nanukko.nanukko_back.repository.chat;

import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {

    Page<ChatMessages> findByChatRoom_ChatRoomIdOrderByCreatedAt(Long chatRoomId, Pageable pageable);

    /*발신자가 보낸 메시지 읽음 처리*/
    @Modifying
    void updateIsReadByTrueAndChatRoom_ChatRoomIdAndSender_UserIdNotAndIsReadFalse(Long chatRoomId,String userId);

    /*최신 메시지 바꾸기*/
    @Modifying//수정 쿼리임을 명시, 반환값으로 영향받은 row 수를 받을 수 있음
    @Query("UPDATE ChatMessages m " +
            "SET m.isLatest = true " +
            "WHERE m.chatRoom.chatRoomId = :chatRoomId " +
            "AND m.createdAt = (SELECT MAX(m2.createdAt) " +
            "FROM ChatMessages m2 " +
            "WHERE m2.chatRoom.chatRoomId = :chatRoomId)")
    int updateChatMessagesLatest(Long chatRoomId); // 성공하면 1 리턴

    /*이전 최신 메시지 다시 최신이 아니게 바꿈*/
    @Modifying
    int updateIsLatestByFalseAndChatRoom_ChatRoomId(Long chatroomId);

    
}

