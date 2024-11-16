package nanukko.nanukko_back.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.notification.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long notificationId;
    private NotificationType type;
    private String url;
    private boolean isRead;
    private String content;
    private LocalDateTime createdAt;
}
