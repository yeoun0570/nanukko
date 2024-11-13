package nanukko.nanukko_back.dto.chat;

import lombok.*;
import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.MessageType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatMessageDTO {

    private Long chatMessageId; //채팅 메시지 ID


    private String sender; //발신자 ID (FK)
    private Long chatRoom; //채팅방 ID (FK)

    private String chatMessage; //채팅 내용

    private LocalDateTime createdAt; //생성 날짜

    private boolean isRead; //읽음 여부 -> true = 읽음, false = 안읽음

    private String image; //이미지

    private boolean isLatest; //최신메시지

    private MessageType type;//일반 메시지인지 여부

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static ChatMessageDTO from(ChatMessages entity) {
        return ChatMessageDTO.builder()
                .chatMessageId(entity.getChatMessageId())
                .sender(entity.getSender().getUserId())  // 또는 getSender().getId()
                .chatMessage(entity.getChatMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .type(entity.getType())
                .build();
    }


}