package nanukko.nanukko_back.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//CORS 설정
@Configuration
@Log4j2
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("**********CORS 설정*************");
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedHeaders("*")
                .exposedHeaders("Authorization")  // Authorization 헤더 노출 설정 추가
                .allowedHeaders("x-amz-acl")
                .maxAge(3600);

        // WebSocket 엔드포인트에 대한 CORS 설정 추가
        registry.addMapping("/ws-stomp/**")// Nuxt 개발 서버 주소
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")// addAllowedOrigin("*")와 함께 사용할 수 없음, 특정 도메인 명시 필요
                .allowCredentials(true)// 모든 HTTP 요청 헤더 허용(사용자 정의 헤더인 Authorization, Content-Type 등 포함)
                .maxAge(3600);

    }
}
