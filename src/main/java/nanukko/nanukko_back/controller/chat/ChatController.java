package nanukko.nanukko_back.controller.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.chat.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/*메시지 가공이나 처리를 해 줄 핸들러*/

@RestController
@RequiredArgsConstructor
@Log4j2
public class ChatController {

    @MessageMapping("/chat/{chatRoomId}")// StompConfig에 설정해놓은 /app과 합쳐져 /app/chat으로 왔을 때 이 컨트롤러 탐
    @SendTo("/topic/{chatRoomId}/message")//핸들러에서 처리를 마친 후 반환 값을 /topic/message의 경로로 메시지를 보냄
    public ChatMessage sendMessage(@DestinationVariable Long chatRoomId, ChatMessage msg) throws InterruptedException {
        Thread.sleep(500);
        System.out.println("채팅 컨트롤러~");
        System.out.println(HtmlUtils.htmlEscape(msg.getChatMessage()));
        return msg;
    }

}
