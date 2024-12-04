package nanukko.nanukko_back.config;

import nanukko.nanukko_back.jwt.CustomLogoutFilter;
import nanukko.nanukko_back.jwt.JWTFilter;
import nanukko.nanukko_back.jwt.JWTUtil;
import nanukko.nanukko_back.jwt.LoginFilter;
import nanukko.nanukko_back.repository.RefreshJWTRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.service.ReissueService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshJWTRepository refreshJWTRepository;
    private final UserRepository userRepository;
    private final ReissueService reissueService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil, RefreshJWTRepository refreshJWTRepository, UserRepository userRepository, ReissueService reissueService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshJWTRepository = refreshJWTRepository;
        this.userRepository = userRepository;
        this.reissueService = reissueService;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /*검증 과정에서 항상 비밀번호를 해시코드로 암호화해서 관리해야 하므로 비밀번호 암호화하는 데 사용되는 인코더를 생성*/
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {//스프링 시큐리티에서 제공하는 비밀번호 암호화 클래스
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource getCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));// 프론트엔드 서버 허용
        //configuration.setAllowedMethods(Collections.singletonList("GET, POST, PUT, DELETE"));// 메소드 허용
        configuration.setAllowCredentials(true);// 프론트서버에서 credentials 설정 해주면 여기도 무조건 true 설정 해줘야 함
        configuration.setAllowedHeaders(Collections.singletonList("*"));// 허용할 헤더 설정
        configuration.setMaxAge(3600L);// 허용 시간 설정

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Cookie", "x-amz-acl"));


        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));// 프론트 전송 시 Authorization 헤더에 JWT를 담아 보낼 것이기 때문에 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/api/**", configuration);  // 모든 API 경로에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/ws-stomp/**", configuration);
        return source;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // LoginFilter URL 설정 추가
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshJWTRepository, userRepository);
        loginFilter.setFilterProcessesUrl("/api/login"); // 실제 로그인 요청을 처리할 URL 명시적 설정

        // 로그인 필터들의 CORS 문제 방지를 위한 설정
        http
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(getCorsConfiguration()))

                ;

        // csrf disable
        http
                .csrf((auth) -> auth.disable());//jwt 방식은 stateless 상태로 관리하기 때문에 csrf공격을 방어하지 않아도 되므로 disable로 설정

        //폼 로그인 disable
        http
                .formLogin((auth) -> auth.disable());//Form 기반 로그인 방식(사용자가 로그인 폼을 제출하면, 서버는 해당 정보를 받아 세션을 생성하고 이증 관리) disable

        http//HTTP Basic인증방식 비활성화
                .httpBasic((auth)-> auth.disable());// 클라이언트 요청 시 사용자 이름과 비밀번호를 인코딩하여 서버에 전달하는 방식, 보안이 취약하며 사용자의 자격 증명을 매번 전송해야해서 JWT방식과 맞지 않음

        // 요청 경로별 권한 설정
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/login", "/api/logout", "/", "/api/register/**", "/api/reissue", "/ws-stomp/**", "/api/user/find-id").permitAll()//적어준 경로에 대해서는 전체 허용
                        .requestMatchers("/api/admin").hasRole("ADMIN")//적어준 경로에는 ADMIN만 접근 가능
                        .requestMatchers("/api/reissue").permitAll() // access 토큰 만료된 상태로 요청하므로 permit all
                        .requestMatchers("/ws-stomp/**").authenticated()  // WebSocket 엔드포인트 허용
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/queue/chat/**").authenticated()
                        .requestMatchers("/api/notice/connect/**").permitAll()
                        .requestMatchers("/api/files/**").authenticated()
                        .requestMatchers("/api/review/**").authenticated()
                        .requestMatchers("/api/upload-dummy-images").permitAll()
                        .requestMatchers("/api/chatbot/**").permitAll()
                        .requestMatchers("/api/products/new").authenticated()
                        .requestMatchers("/api/products/main").authenticated()
                        .requestMatchers("/api/wishlist/{productId}").authenticated()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/wishlist/**").permitAll()
                        .anyRequest().authenticated()//나머지 요청에 대해서는 로그인 한 사용자들만 접근 가능함
                );

        // JWTFilter를 LoginFilter 앞에 달아줌
        http
                .addFilterBefore(new JWTFilter(jwtUtil, reissueService), LoginFilter.class);

        // 커스텀 필터 추가
        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        // loginFilter를 통해서 검증 하고, 검증 성공하면 jwtUtil을 통해서 jwt를 생성 후 다시 loginFilter에 구현된 로그인 성공 후 실행될 메소드에 jwt 반환해준다.


        // 커스텀 로그아웃 필터 추가
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshJWTRepository), LogoutFilter.class);

        // 세션 stateless 상태로 관리
        http
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
