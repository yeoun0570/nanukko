package nanukko.nanukko_back.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MailConfig {
    @Value("${spring.mail.username}")
    private String serverEmail;
}
