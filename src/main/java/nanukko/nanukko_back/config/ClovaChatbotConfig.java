package nanukko.nanukko_back.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ClovaChatbotConfig {

    @Value("${clova.chatbot.url}")
    private String chatbotUrl; //네이버 클로버 챗봇 API 엔드포인트 URL

    @Value("${clova.chatbot.secret}")
    private String secretKey; //API 인증을 위한 시크릿 키
}