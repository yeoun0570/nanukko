package nanukko.nanukko_back.dto.chat;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 읽지 않은 메시지 요약 정보를 담는 DTO
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UnreadMessageSummary {
    private Long chatRoomId;
    private String productName;
    private String thumbnailImage;
    private String senderName;
    private int unreadCount;
    private LocalDateTime lastMessageTime;// 마지막 메시지 시간
}
