package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.Delivery;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import nanukko.nanukko_back.domain.order.DeliveryWebhook;
import nanukko.nanukko_back.repository.DeliveryRepository;
import nanukko.nanukko_back.repository.DeliveryWebhookRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeliveryWebhookService {
    private final DeliveryWebhookRepository webhookRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;
    private static final int MAX_RETRY_COUNT = 3; //최대 재연결 횟수

    //웹훅 수신 및 저장
    @Transactional
    public void receiveWebhook(String webhookId, String trackingNumber,
                               DeliveryStatus status, String rawPayload) {
        //중복 웹훅 체크
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("중복된 웹훅 수신: {}", webhookId);
            return;
        }

        try {
            //배송 정보 조회
            Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber).
                    orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

            //웹훅 저장
            DeliveryWebhook webhook = DeliveryWebhook.builder()
                    .delivery(delivery)
                    .webhookId(webhookId)
                    .trackingNumber(trackingNumber)
                    .status(status)
                    .rawPayload(rawPayload)
                    .processed(false)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            webhookRepository.save(webhook);

            //비동기로 웹훅 처리 시작
            processWebhookAsync(webhook);
        } catch (Exception e) {
            log.error("웹훅 저장 실패 - webhookId: {}", webhookId, e);
        }
    }

    @Async // 비동기 처리를 위한 어노테이션
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션 시작
    public void processWebhookAsync(DeliveryWebhook webhook) {
        processWebhook(webhook);
    }


    //웹훅 처리
    @Transactional
    public void processWebhook(DeliveryWebhook webhook) {
        try {
            //배송상태 업데이트
            deliveryService.updateDeliveryStatusFromWebhook(
                    webhook.getTrackingNumber(),
                    webhook.getStatus()
            );

            //처리 완료 표시
            webhook.markAsProcessed();
            webhookRepository.save(webhook);
        } catch (Exception e) {
            log.error("웹훅 처리 실패 - webhookId: {}", webhook.getWebhookId(), e);
            //재시도 횟수 증가
            webhook.incrementRetryCount();
            webhookRepository.save(webhook);
        }
    }

    //미처리된 웹훅 재처리
    @Scheduled(fixedRate = 5 * 60 * 1000) //5분마다
    @Transactional
    public void retryFailedWebhooks() {
        // 처리 실패한 웹훅 중 최대 재시도 횟수를 넘지 않은 것들 조회
        List<DeliveryWebhook> failedWebhooks =
                webhookRepository.findByProcessedFalseAndRetryCountLessThanOrderByCreatedAt(MAX_RETRY_COUNT);

        // 각각의 실패한 웹훅에 대해 재처리 시도
        for (DeliveryWebhook webhook : failedWebhooks) {
            try {
                processWebhookAsync(webhook);
                log.info("웹훅 재처리 성공 - webhookId: {}", webhook.getWebhookId());
            } catch (Exception e) {
                log.error("웹훅 재처리 실패 - webhookId: {}", webhook.getWebhookId(), e);
            }
        }
    }

}
