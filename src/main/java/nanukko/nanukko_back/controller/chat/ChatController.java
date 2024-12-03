package nanukko.nanukko_back.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.chat.ChatMessageDTO;
import nanukko.nanukko_back.dto.chat.ChatRoomDTO;
import nanukko.nanukko_back.dto.page.PageResponseDTO;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.service.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // JSON 형태로 데이터를 반환하는 API 컨트롤러
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
@Log4j2
@RequestMapping("/api/chat") // 이 컨트롤러의 기본 URL 경로
public class ChatController {
    private final ChatService chatService;

    /*채팅방 목록 불러오기 -> 기존 메시지는 HTTP 요청으로 한 번에 가져옴 */
    @GetMapping("/list")
    public ResponseEntity<PageResponseDTO<ChatRoomDTO>> getChatRoomList(
            @AuthenticationPrincipal CustomUserDetails details,
            @RequestParam(defaultValue = "0") int page, // 페이지 번호(기본값 0)
            @RequestParam(defaultValue = "30") int size // 한 페이지당 데이터 수(기본값 30)
    ){

        String userId = details.getUsername();
        Pageable pageable = PageRequest.of(page, size);// PageRequest.of(): JPA에서 페이징을 위한 객체 생성
        PageResponseDTO<ChatRoomDTO> chatRooms = chatService.getChatRooms(userId, pageable);// chatService.getChatRooms(): 실제 데이터 조회 로직
        return ResponseEntity.ok(chatRooms);// ResponseEntity.ok(): HTTP 200 응답과 함께 데이터 반환
    }

    /*채팅방 생성/입장*/
    @PostMapping("/getChat")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestParam Long productId,
            @AuthenticationPrincipal CustomUserDetails details,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        String userId = details.getUsername();
        Pageable pageable = PageRequest.of(page, size);
        ChatRoomDTO chatRoomDTO = chatService.createOrReturnToChatRoom(userId, productId, pageable);
        return ResponseEntity.ok(chatRoomDTO);
    }

    @GetMapping("/messages")
    public ResponseEntity<PageResponseDTO<ChatMessageDTO>> getChatMessages(
            @RequestParam Long roomId,
            @AuthenticationPrincipal CustomUserDetails details,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        String userId = details.getUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<ChatMessageDTO> messages = chatService.getChatMessagesAndMarkAsRead(
                roomId, userId, pageable
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/getMessages")
    public ResponseEntity<PageResponseDTO<ChatMessageDTO>> getChatRoomMessages(
            @RequestParam Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails details,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = details.getUsername();
        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<ChatMessageDTO> messages = chatService.getChatMessagesAndMarkAsRead(
                chatRoomId, userId, pageable
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread-messages")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        List<ChatMessageDTO> unreadMessages = chatService.getUnreadMessages(userId);
        log.info("안 읽은 메시지 {}:", unreadMessages);
        return ResponseEntity.ok(unreadMessages);
    }

}