package nanukko.nanukko_back.controller.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.chat.ChatMessageDTO;
import nanukko.nanukko_back.dto.chat.ChatRoomDTO;
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

@Controller // WebSocket 메시지 처리를 위한 컨트롤러
@RequiredArgsConstructor
@Log4j2
public class ChatMessageController {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;// 해당 객체를 통해 메시지 브로커로 데이터를 전달한다.

    /*메시지 전송*/
    @MessageMapping("/chat/{chatRoomId}") // StompConfig에 설정해놓은 /app과 합쳐져 /app/chat으로 왔을 때 이 컨트롤러 탐(클라이언트에서 메시지를 보낼 주소)
    @SendTo("/queue/chat/{chatRoomId}") //핸들러에서 처리를 마친 후 반환 값을 /topic/message의 경로로 메시지를 보냄(메시지를 구독중인 클라이언트들에게 브로드캐스트)
    public ChatMessageDTO sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO msg
    ){
        //Thread.sleep(500); // 메시지 처리 시간 시뮬레이션
        log.info("채팅 메시지 전송: roomId={}, message={}", chatRoomId, msg.getChatMessage());

        return chatService.sendMessage(chatRoomId, msg); // 메시지 전송 + DB 저장
    }

    /*채팅방 입장*/
    // 채팅방의 이전 메시지 내역 조회 + 안 읽은 메시지 읽음 처리 (채팅방 첫 입장 시 호출)
    @MessageMapping("/chat/enter/{chatRoomId}")
    @SendTo("/queue/chat/{chatRoomId}")
    public PageResponseDTO<ChatMessageDTO> enterAndGetChatMessages(
            @DestinationVariable Long chatRoomId,
            @Payload Map<String, Object> payload  // DTO 대신 Map으로 받기
    ) {
        String userId = (String) payload.get("userId");
        int page = payload.containsKey("page") ? (Integer) payload.get("page") : 0;
        int size = payload.containsKey("size") ? (Integer) payload.get("size") : 50;

        Pageable pageable = PageRequest.of(page, size);
        return chatService.getChatMessagesAndMarkAsRead(chatRoomId, userId, pageable);
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
            @Payload ReadMessageRequest request
    ) {
        try {
            log.info("실시간 읽음 처리 요청: roomId={}, messageIds={}", chatRoomId, request.getMessageIds());

            // 페이징 객체 생성
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

            // 서비스 호출 - 읽음 처리 및 업데이트된 메시지 목록 반환
            PageResponseDTO<ChatMessageDTO> updatedMessages =
                    chatService.markMessagesAsReadRealtime(chatRoomId, request.getUserId(), request.getMessageIds(), pageable);

            // 업데이트된 메시지 정보 반환
            ChatMessageDTO response = ChatMessageDTO.builder()
                    .chatMessageId(null)
                    .messageIds(request.getMessageIds())
                    .updatedMessages(updatedMessages.getContent())
                    .isRead(true)
                    .build();

            log.info("읽음 처리 완료: {}", response);
            return response;

        } catch (Exception e) {
            log.error("읽음 처리 실패", e);
            throw e;
        }
    }

    // 수정된 요청 데이터 클래스
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ReadMessageRequest {
        private List<Long> messageIds; // 여러 메시지 ID를 받을 수 있도록 수정
        private String userId;
        private int page;
        private int size;
    }

}