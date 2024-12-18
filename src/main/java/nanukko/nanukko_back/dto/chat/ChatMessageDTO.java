package nanukko.nanukko_back.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import nanukko.nanukko_back.domain.chat.ChatMessages;
import nanukko.nanukko_back.domain.chat.MessageType;

import java.time.LocalDateTime;
import java.util.List;

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

    private String senderName;      // 발신자 이름 추가
    private String productName;     // 상품명 추가
    private String productThumbnail;// 상품 썸네일 추가

    // JSON 직렬화시 필드명을 'messageType'으로 사용
    @JsonProperty("messageType")
    private MessageType type;

    // 읽음 처리를 위한 추가 필드
    private List<Long> messageIds; // 읽음 처리된 메시지 ID 목록
    private List<ChatMessageDTO> updatedMessages; // 업데이트된 메시지 목록

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
//    public static ChatMessageDTO from(ChatMessages entity) {
//        return ChatMessageDTO.builder()
//                .chatMessageId(entity.getChatMessageId())
//
//                .sender(entity.getSender().getUserId())  // 또는 getSender().getId()
//                .sender(entity.getSender().getAddress().getAddrMain())
//                .sender(entity.getSender().getAddress().getAddrDetail())
//                .sender(entity.getSender().getAddress().getAddrZipcode())
//
//                .chatMessage(entity.getChatMessage())
//                .createdAt(entity.getCreatedAt())
//                .isRead(entity.isRead())
//                .type(entity.getType())
//                .image(entity.getImage())
//                .isLatest(entity.isLatest())
//                .build();
//    }
    public static ChatMessageDTO from(ChatMessages entity) {
        return ChatMessageDTO.builder()
                .chatMessageId(entity.getChatMessageId())
                .chatRoom(entity.getChatRoom().getChatRoomId())
                .sender(entity.getSender().getUserId())
                .senderName(entity.getSender().getNickname())
                .productName(entity.getChatRoom().getProduct().getProductName())
                .productThumbnail(entity.getChatRoom().getProduct().getThumbnailImage())
                .chatMessage(entity.getChatMessage())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isRead())
                .type(entity.getType())
                .image(entity.getImage())
                .isLatest(entity.isLatest())
                .build();
    }
}