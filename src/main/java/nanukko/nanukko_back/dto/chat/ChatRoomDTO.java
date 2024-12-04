package nanukko.nanukko_back.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import nanukko.nanukko_back.domain.chat.ChatRoom;
import nanukko.nanukko_back.domain.product.ProductStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatRoomDTO {
    private Long chatRoomId;                 // 채팅방 ID

    private Long productId;                  // 상품 ID
    private String productName;              // 상품 이름 (클라이언트 표시용)
    private String productThumbnail;         // 상품 사진
    private int price;                       // 상품 가격
    private String status;            // 상품 상태 (SELLING, RESERVED, SOLD_OUT, REMOVED)

    private String buyerId;                  // 구매자 ID
    private String buyerName;                // 구매자 이름 (클라이언트 표시용)
    private String buyerProfile;             // 구매자 프로필 사진

    private String sellerId;                 // 판매자 ID
    private String sellerName;               // 판매자 이름 (클라이언트 표시용)
    private String sellerProfile;            // 판매자 프로필 사진

    private List<ChatMessageDTO> chatMessages; // 채팅 메시지 목록

    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;         // 채팅방 생성 일시

    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;         // 채팅방 수정 일시

    // 나가기 시간 정보
    private LocalDateTime sellerLeftAt;      // 판매자가 나간 시간 (null이면 참여 중)
    private LocalDateTime buyerLeftAt;       // 구매자가 나간 시간 (null이면 참여 중)

    /**
     * ChatRoom 엔티티를 ChatRoomDTO로 변환하는 정적 팩토리 메서드
     * @param entity 변환할 ChatRoom 엔티티
     * @return 변환된 ChatRoomDTO
     */
    public static ChatRoomDTO from(ChatRoom entity) {
        return ChatRoomDTO.builder()
                .chatRoomId(entity.getChatRoomId())
                // 상품 정보
                .productId(entity.getProduct().getProductId())
                .productName(entity.getProduct().getProductName())  // 상품 이름 추가
                .price(entity.getProduct().getPrice())//상품 가격
                .status(entity.getProduct().getStatus().name())
                .productThumbnail(entity.getProduct().getThumbnailImage()) //사진
                // 구매자 정보
                .buyerId(entity.getBuyer().getUserId())
                .buyerName(entity.getBuyer().getNickname())
                .buyerProfile(entity.getBuyer().getProfile())
                // 판매자 정보
                .sellerId(entity.getProduct().getSeller().getUserId())
                .sellerName(entity.getProduct().getSeller().getNickname())
                .sellerProfile(entity.getProduct().getSeller().getProfile())
                // 채팅 메시지 (있는 경우에만 변환)
                .chatMessages(entity.getChatMessages() != null ?
                        entity.getChatMessages().stream()
                                .map(ChatMessageDTO::from)
                                .collect(Collectors.toList()) : null)
                // 시간 정보
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .sellerLeftAt(entity.getSellerLeftAt())
                .buyerLeftAt(entity.getBuyerLeftAt())
                .build();
    }

    /**
     * 특정 사용자가 해당 채팅방에서 나갔는지 확인하는 메서드
     * @param userId 확인할 사용자 ID
     * @return 나갔으면 true, 참여 중이면 false
     */
    public boolean isLeft(String userId) {
        if (userId.equals(sellerId)) {
            return sellerLeftAt != null;
        } else if (userId.equals(buyerId)) {
            return buyerLeftAt != null;
        }
        return false;
    }

    /**
     * 채팅방의 상대방 ID를 반환하는 메서드
     * @param userId 현재 사용자 ID
     * @return 상대방 ID
     */
    public String getOtherUserId(String userId) {
        return userId.equals(sellerId) ? buyerId : sellerId;
    }

    /**
     * 채팅방의 상대방 이름을 반환하는 메서드
     * @param userId 현재 사용자 ID
     * @return 상대방 이름
     */
    public String getOtherUserName(String userId) {
        return userId.equals(sellerId) ? buyerName : sellerName;
    }
}