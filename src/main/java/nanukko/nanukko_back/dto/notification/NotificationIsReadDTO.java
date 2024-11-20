package nanukko.nanukko_back.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationIsReadDTO {
    private Long notificationId;
    private boolean isRead;
}
