package nanukko.nanukko_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import nanukko.nanukko_back.dto.order.DeliveryRegistrationDTO;
import nanukko.nanukko_back.dto.order.DeliveryResponseDTO;
import nanukko.nanukko_back.dto.order.DeliveryUpdateStatusDTO;
import nanukko.nanukko_back.exception.ErrorResponse;
import nanukko.nanukko_back.service.DeliveryService;
import nanukko.nanukko_back.service.DeliveryWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> registerDelivery(@Valid @RequestBody DeliveryRegistrationDTO request) {
        try {
            log.info("운송장 등록 요청 - productId: {}, orderId: {}, carrierId: {}, trackingNumber: {}",
                    request.getProductId(), request.getOrderId(), request.getCarrierId(), request.getTrackingNumber());

            DeliveryResponseDTO response = deliveryService.registerDelivery(request);

            log.info("운송장 등록 완료 - deliveryId: {}", response.getDeliveryId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("운송장 등록 실패: ", e);  // 에러 스택트레이스 출력
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
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

    /////////////////////////테스트를 위한 배송상태 업데이트
    @PostMapping("/update-status")
    public ResponseEntity<DeliveryUpdateStatusDTO> updateDeliveryStatus(
            @RequestBody DeliveryUpdateStatusDTO dto
            ) {
        DeliveryUpdateStatusDTO response = deliveryService.updateDeliveryStatus(dto);
        return ResponseEntity.ok(response);
    }

}
