package nanukko.nanukko_back.dto.chat;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatMessage {

    private Long chatMessageId; //채팅 메시지 ID


    private String sender; //발신자 ID (FK)
    private Long chatRoom; //채팅방 ID (FK)

    private String chatMessage; //채팅 내용

    private LocalDateTime createdAt; //생성 날짜

    private boolean isRead; //읽음 여부 -> true = 읽음, false = 안읽음

    private String image; //이미지

    private boolean isLatest; //최신메시지


}