package nanukko.nanukko_back.dto.chat;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatRoom {

    private Long chatRoomId=1L; //채팅방 ID

    private Long product; // 상품 ID (FK)

    private String buyer; // 구매자 ID (FK)

    private String seller;// 판매자 id


    private List<ChatMessage> chatMessages; //채팅메시지 ID (FK)

    private LocalDateTime createdAt; //생성 날짜

    private LocalDateTime updatedAt; //수정 날짜

    private boolean isSellerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감

    private boolean isBuyerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감


}