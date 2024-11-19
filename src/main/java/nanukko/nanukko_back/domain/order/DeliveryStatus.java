package nanukko.nanukko_back.domain.order;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PREPARING("배송준비중"),
    IN_TRANSIT("배송중"),
    DELIVERED("배송완료");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }
}