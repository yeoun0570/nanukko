package nanukko.nanukko_back.repository.notification;

import nanukko.nanukko_back.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 알림 받는 사람 아이디 찾기
    List<Notification> findByReceiverUserIdOrderByCreatedAtDesc(String userId);
}
