package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String> {
    //에스크로 상태가 10일 뒤에 자동으로 구매확정 시키기 위해 필요한 메서드
    List<Orders> findByStatusAndEscrowDeadlineBefore(
            PaymentStatus status,
            LocalDateTime deadline
    );

    //마이페이지 구매 상품을 페이징처리하여 조회하기 위해 사용될 메서드
    Page<Orders> findByBuyerAndStatusOrderByCreatedAtDesc(User seller, PaymentStatus status, Pageable pageable);
}
