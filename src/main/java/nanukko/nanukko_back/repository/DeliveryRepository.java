package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.order.Delivery;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    // 배송 상태로 배송 찾기
    List<Delivery> findByStatus(DeliveryStatus status);

    // 주문 번호로 배송 찾기
    Optional<Delivery> findByOrderOrderId(String orderId);
    
    //운송장 번호로 배송 찾기
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
}
