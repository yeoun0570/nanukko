package nanukko.nanukko_back.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentsConfig {
    @Value("${toss.payments.secret-key}")
    private String secretKey;
}
