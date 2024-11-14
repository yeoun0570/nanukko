package nanukko.nanukko_back.controller.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.chat.ChatRoom;
import nanukko.nanukko_back.dto.chat.ChatMessageDTO;
import nanukko.nanukko_back.dto.chat.ChatRoomDTO;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.service.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.nio.file.AccessDeniedException;

/*메시지 가공이나 처리를 해 줄 핸들러*/

@RestController // JSON 형태로 데이터를 반환하는 API 컨트롤러
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
@Log4j2
@RequestMapping("/api/chat") // 이 컨트롤러의 기본 URL 경로
public class ChatController {

    private final ChatService chatService;


    /*메시지 전송*/
    @MessageMapping("/chat/{chatRoomId}")// StompConfig에 설정해놓은 /app과 합쳐져 /app/chat으로 왔을 때 이 컨트롤러 탐(클라이언트에서 메시지를 보낼 주소)
    @SendTo("/topic/{chatRoomId}/message")//핸들러에서 처리를 마친 후 반환 값을 /topic/message의 경로로 메시지를 보냄(메시지를 구독중인 클라이언트들에게 브로드캐스트)
    public ChatMessageDTO sendMessage(@DestinationVariable Long chatRoomId, ChatMessageDTO msg) throws InterruptedException {
        Thread.sleep(500);
        log.info("채팅 메시지 전송: roomId={}, message={}", chatRoomId, msg.getChatMessage());

        return chatService.sendMessage(chatRoomId, msg);////메시지 전송 + DB 저장
    }

    /*채팅방 입장*/
    // 채팅방의 이전 메시지 내역 조회 + 안 읽은 메시지 읽음 처리 (채팅방 첫 입장 시 호출)
    // 채팅방 입장 시 메시지 조회 및 읽음 처리
    @MessageMapping("/chat/enter/{chatRoomId}")
    @SendTo("/topic/chat.{chatRoomId}")
    public PageResponseDTO<ChatMessageDTO> enterAndGetChatMessages(//@AuthenticationPrincipal UserDetails userDetails  // 현재 로그인한 사용자(시큐리티)
                                                                           @RequestParam String userId,
                                                                   @DestinationVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<ChatMessageDTO> messages = chatService.getChatMessagesAndMarkAsRead(chatRoomId,userId, pageable);
        return messages;
    }

    /*채팅방 목록 불러오기*/
    @GetMapping("/list")
    public ResponseEntity<PageResponseDTO<ChatRoomDTO>> getChatRoomList(//@AuthenticationPrincipal UserDetails userDetails  // 현재 로그인한 사용자(시큐리티)
                                                                       @RequestParam String userId,
                                                                        @RequestParam(defaultValue = "0")int page, // 페이지 번호(기본값 0)
                                                                        @RequestParam(defaultValue = "30")int size // 한 페이지당 데이터 수(기본값 30)
    ){
        Pageable pageable = PageRequest.of(page, size);// PageRequest.of(): JPA에서 페이징을 위한 객체 생성
        PageResponseDTO<ChatRoomDTO> chatRooms = chatService.getChatRooms(userId, pageable);// chatService.getChatRooms(): 실제 데이터 조회 로직
        return ResponseEntity.ok(chatRooms);// ResponseEntity.ok(): HTTP 200 응답과 함께 데이터 반환
    }

    /*새 채팅방 생성*/
    @PostMapping("newChat")
    public ResponseEntity<ChatRoomDTO> createChatRoom(//@AuthenticationPrincipal UserDetails userDetails  // 현재 로그인한 사용자(시큐리티)
                                                      @RequestParam Long productId,
                                                      @RequestParam String userId

    ){
        ChatRoomDTO chatRoomDTO = chatService.getOrCreateChatRoom(userId, productId);
        return ResponseEntity.ok(chatRoomDTO);
    }

    /**
     * 채팅방 나가기
     */
    @MessageMapping("/chat/leave")
    @SendTo("/topic/chat.{roomId}")
    public ChatMessageDTO leaveRoom(@DestinationVariable Long chatRoomId,
                                    String userId) throws AccessDeniedException {
        return chatService.leaveChatRoom(chatRoomId, userId);
    }

    /*채팅방 삭제*/
//    @DeleteMapping("/api/chat/rooms/{roomId}")
//    public ResponseEntity<Void> deleteRoom(
//            @PathVariable Long roomId,
//            //@AuthenticationPrincipal UserDetails userDetails
//            String userId) {
//        chatService.deleteChatRoom(roomId, userId);
//        return ResponseEntity.ok().build();
//    }



}
