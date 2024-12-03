package nanukko.nanukko_back.controller.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.WebSocketSessionManager;
import nanukko.nanukko_back.dto.chat.ChatMessageDTO;
import nanukko.nanukko_back.dto.chat.ChatRoomDTO;
import nanukko.nanukko_back.dto.chat.ReadMessageRequest;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.service.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller // WebSocket 메시지 처리를 위한 컨트롤러
@RequiredArgsConstructor
@Log4j2
public class ChatMessageController {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;// 해당 객체를 통해 메시지 브로커로 데이터를 전달한다.
    private final WebSocketSessionManager sessionManager;

    /*메시지 전송*/
    @MessageMapping("/chat/{chatRoomId}") // StompConfig에 설정해놓은 /app과 합쳐져 /app/chat으로 왔을 때 이 컨트롤러 탐(클라이언트에서 메시지를 보낼 주소)
    @SendTo("/queue/chat/{chatRoomId}") //핸들러에서 처리를 마친 후 반환 값을 /topic/message의 경로로 메시지를 보냄(메시지를 구독중인 클라이언트들에게 브로드캐스트)
    public ChatMessageDTO sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO msg,
            Principal principal
    ) {
        //        //Thread.sleep(500); // 메시지 처리 시간 시뮬레이션
        log.info("채팅 메시지 전송: roomId={}, message={}", chatRoomId, msg.getChatMessage());

        return chatService.sendMessage(chatRoomId, msg); // 메시지 전송 + DB 저장
    }
//
//
//        // 서비스에서는 메시지 저장만 처리
//        ChatMessageDTO savedMessage = chatService.sendMessage(chatRoomId, msg);
//
//        // 상대방에게 알림 전송
//        String recipientId = chatService.getRecipientId(chatRoomId, msg.getSender());
//        simpMessagingTemplate.convertAndSendToUser(
//                recipientId,
//                "/queue/notifications",
//                savedMessage
//        );
//
//        return savedMessage;  // @SendTo에 의해 채팅방으로 전송됨
//    }

    /*채팅방 입장*/
    // 채팅방의 이전 메시지 내역 조회 + 안 읽은 메시지 읽음 처리 (채팅방 첫 입장 시 호출)
    @MessageMapping("/chat/enter/{chatRoomId}")
    @SendTo("/queue/chat/{chatRoomId}")
    public PageResponseDTO<ChatMessageDTO> enterAndGetChatMessages(
            @DestinationVariable Long chatRoomId,
            @Payload Map<String, Object> payload  // DTO 대신 Map으로 받기
            ,Principal principal
    ) {
        String userId = principal.getName();
        int page = payload.containsKey("page") ? (Integer) payload.get("page") : 0;
        int size = payload.containsKey("size") ? (Integer) payload.get("size") : 50;

        //채팅방 입장 시 읽음 처리 수행
        chatService.handleChatRoomEnter(chatRoomId, userId);

        return chatService.getChatMessagesAndMarkAsRead(chatRoomId, userId, PageRequest.of(page, size));
    }

    /*채팅방 나가기*/
    @MessageMapping("/chat/leave/{chatRoomId}")
    @SendTo("/queue/chat/{chatRoomId}")
    public ResponseEntity<PageResponseDTO<ChatRoomDTO>> leaveRoom(
            @DestinationVariable Long chatRoomId,
            @Payload Map<String, Integer> pageInfo,
            @Header("simpUser") Principal principal
            ) throws AccessDeniedException {
        String userId = principal.getName();
        chatService.leaveChatRoom(chatRoomId, userId); // 채팅방 나감 처리 후
        Pageable pageable = PageRequest.of(pageInfo.get("page"), pageInfo.get("size"));
        PageResponseDTO<ChatRoomDTO> chatRooms = chatService.getChatRooms(userId, pageable); // 나간 상태의 채팅방 목록 반환
        return ResponseEntity.ok(chatRooms);
    }

    @MessageMapping("/chat/{chatRoomId}/mark-read")
    @SendTo("/queue/{chatRoomId}")
    public void markMessageAsRead(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO messageDTO
    ) {
        chatService.markMessageAsRead(chatRoomId, messageDTO.getSender(), messageDTO.getChatMessageId());
    }

    /*여러 메시지 읽음 처리*/
    @MessageMapping("/chat/{chatRoomId}/read-realtime")
    @SendTo("/queue/chat/{chatRoomId}")
    public ChatMessageDTO markMessageAsReadRealtime(
            @DestinationVariable Long chatRoomId,
            @Header("simpUser") Principal principal,
            @Payload ReadMessageRequest request
    ) {
        String userId = principal.getName();
        log.info("읽음 처리 요청 - chatRoomId: {}, userId: {}", chatRoomId, userId);

        PageResponseDTO<ChatMessageDTO> result = chatService.markMessagesAsReadRealtime(
                chatRoomId,
                userId,
                request.getMessageIds(),
                PageRequest.of(request.getPage(), request.getSize())
        );

        return ChatMessageDTO.builder()
                .messageIds(request.getMessageIds())
                .updatedMessages(result.getContent())
                .isRead(true)
                .build();
    }

}