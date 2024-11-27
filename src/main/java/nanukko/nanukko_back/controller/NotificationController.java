package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.notification.NotificationIsReadDTO;
import nanukko.nanukko_back.dto.notification.NotificationResponseDTO;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
@Log4j2
public class NotificationController {
    private final NotificationService notificationService;

    //Last-Event-ID는 SSE 연결이 끊어졌을 경우, 클라가 수신한 마지막 이벤트의 id값을 의미. 항상 존재하는 것은 아니기 때문에 false
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @RequestParam String userId,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        log.info("SSE 연결 요청 - userId: {}, lastEventId: {}", userId, lastEventId);
        try {
            SseEmitter emitter = notificationService.subscribe(userId, lastEventId);
            log.info("SSE 연결 성공 - userId: {}", userId);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("SSE 연결 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw e;
        }
    }

    // 이전 알림 조회
    @GetMapping("/previous")
    public List<NotificationResponseDTO> getPreviousNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        log.info("이전 알림 조회 요청 - userId: {}", userId); // 로그 추가
        List<NotificationResponseDTO> notifications = notificationService.getPreviousNotifications(userId);
        log.info("조회된 알림 수: {}", notifications.size()); // 로그 추가
        return notifications;
    }

    // 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationIsReadDTO> markAsReadNotification(
            @PathVariable Long notificationId) {
        NotificationIsReadDTO response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(response);
    }

    // 알림 모두 읽음 처리
    @PostMapping("/read-all")
    public ResponseEntity<List<NotificationIsReadDTO>> markAllAsReadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUsername();
        List<NotificationIsReadDTO> response = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(response);
    }

    // 알림 삭제
    @PostMapping("/remove")
    public ResponseEntity<Void> removeNotice(
            @RequestParam Long notificationId
    ) {
        notificationService.removeNotice(notificationId);
        return ResponseEntity.ok().build();
    }

    // 알림 모두 삭제
    @PostMapping("/removeAll")
    public ResponseEntity<Void> removeAllNotice(
            @RequestBody List<Long> notificationIds
    ) {
        notificationService.removeAllNotice(notificationIds);
        return ResponseEntity.ok().build();
    }
}
