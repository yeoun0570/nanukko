package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import nanukko.nanukko_back.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
public class NotificationController {
    private final NotificationService notificationService;

    //Last-Event-ID는 SSE 연결이 끊어졌을 경우, 클라가 수신한 마지막 이벤트의 id값을 의미. 항상 존재하는 것은 아니기 때문에 false
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
//            @AuthenticationPrincipal UserDetails userDetails
            @RequestParam String userId,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        SseEmitter emitter = notificationService.subscribe(userId, lastEventId);
        return ResponseEntity.ok(emitter);
    }
}
