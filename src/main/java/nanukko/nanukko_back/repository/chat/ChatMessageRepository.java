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

    
}

