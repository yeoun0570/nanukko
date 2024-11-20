package nanukko.nanukko_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import nanukko.nanukko_back.dto.order.DeliveryRegistrationDTO;
import nanukko.nanukko_back.dto.order.DeliveryResponseDTO;
import nanukko.nanukko_back.service.DeliveryService;
import nanukko.nanukko_back.service.DeliveryWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final ObjectMapper objectMapper;
    private final DeliveryService deliveryService;
    private final DeliveryWebhookService webhookService;

    // 판매자가 운송장 등록
    @PostMapping("/register")
    public ResponseEntity<DeliveryResponseDTO> registerDelivery(
            @RequestBody DeliveryRegistrationDTO request) {
        //배송 등록 처리 및 결과 반환
        return ResponseEntity.ok(deliveryService.registerDelivery(request));
    }

    // 웹훅 수신
    @PostMapping("/webhook")
    public ResponseEntity<String> handleDeliveryWebhook(
            @RequestBody Map<String, Object> payload
    ) {
        try {
            // webhookId를 페이로드에서 추출 (tracker.delivery 에서 보내는 고유ID)
            String webhookId = (String) payload.get("id");
            // webhook id가 없다면 UUID 생성 (fallback)
            if (webhookId == null) {
                webhookId = UUID.randomUUID().toString();
                log.warn("Webhook ID not found in payload, generated new one: {}", webhookId);
            }
            //운송장 번호 추출
            String trackingNumber = (String) payload.get("trackingNumber");
            //배송 상태 추출
            String state = (String) payload.get("state");

            //배송 상태 반환
            DeliveryStatus status = switch (state) {
                case "in_transit" -> DeliveryStatus.IN_TRANSIT;
                case "delivered" -> DeliveryStatus.DELIVERED;
                default -> null;
            };

            // 유효한 상태일 경우에만 처리
            if (status != null) {
                webhookService.receiveWebhook(
                        webhookId,
                        trackingNumber,
                        status,
                        objectMapper.writeValueAsString(payload)
                );
            }

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("웹훅 처리 실패", e);
            return ResponseEntity.status(500).body("failed");
        }
    }
}
