package nanukko.nanukko_back.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatRoomDTO {

    private Long chatRoomId; //채팅방 ID

    private Long productId;
    private String buyerId;
    private String sellerId;


    private List<ChatMessageDTO> chatMessages; //채팅메시지 ID (FK)

    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt; //생성 날짜

    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt; //수정 날짜

    private boolean isSellerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감

    private boolean isBuyerLeaved; //나가기 여부,null 허용, true면 판매자 나감, false면 구매자 나감


}