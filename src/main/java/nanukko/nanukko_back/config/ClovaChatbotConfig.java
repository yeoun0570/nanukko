package nanukko.nanukko_back.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ClovaChatbotConfig {

    @Value("${clova.chatbot.url}")
    public static String chatbotUrl;

    @Value("${clova.chatbot.secret}")
    public static String secretKey;
}
