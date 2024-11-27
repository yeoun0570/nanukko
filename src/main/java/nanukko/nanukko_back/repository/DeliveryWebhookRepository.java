package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.order.DeliveryWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryWebhookRepository extends JpaRepository<DeliveryWebhook, Long> {
    //미처리된 웹훅 조회
    List<DeliveryWebhook> findByProcessedFalseAndRetryCountLessThanOrderByCreatedAt(int maxRetires);

    //중복 웹훅 체크
    boolean existsByWebhookId(String webhookId);
}
