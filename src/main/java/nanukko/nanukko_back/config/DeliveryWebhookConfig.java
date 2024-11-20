package nanukko.nanukko_back.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DeliveryWebhookConfig {
    @Value("${delivery.tracker.client-id}")
    private String clientId;
    @Value("${delivery.tracker.client-secret}")
    private String clientSecret;
    @Value("${delivery.webhook.callback-url}")
    private String callbackUrl;
}
