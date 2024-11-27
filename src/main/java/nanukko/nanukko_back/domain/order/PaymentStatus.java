package nanukko.nanukko_back.domain.order;

public enum PaymentStatus {
    READY,              // 결제 생성 초기 상태
    IN_PROGRESS,        // 인증 완료, 결제 승인 전
    WAITING_FOR_DEPOSIT,// 가상계좌 입금 대기
    DONE,              // 결제 승인 완료
    CANCELED,          // 결제 취소
    PARTIAL_CANCELED,   // 부분 취소
    ABORTED,           // 결제 승인 실패
    EXPIRED,           // 유효시간 만료로 취소됨

    // 에스크로 추가 상태
    ESCROW_HOLDING,    // 에스크로 보관 중 (DONE 상태에서 전환)
    ESCROW_RELEASED,    // 구매확정으로 판매자 지급 완료

    // 배송 중인 상태
    IN_DELIVERY, //배송중
    DELIVERED //배송완료
}
