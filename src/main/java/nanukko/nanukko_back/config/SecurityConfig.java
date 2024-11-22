package nanukko.nanukko_back.config;

import jakarta.servlet.http.HttpServletRequest;
import nanukko.nanukko_back.jwt.JWTFilter;
import nanukko.nanukko_back.jwt.JWTUtil;
import nanukko.nanukko_back.jwt.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        // 로그인 필터들의 CORS 문제 방지를 위한 설정
        http
                .cors((cors) ->
                        cors.configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration configuration = new CorsConfiguration();

                                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));// 프론트엔드 서버 허용
                                configuration.setAllowedMethods(Collections.singletonList("GET, POST, PUT, DELETE"));// 메소드 허용
                                configuration.setAllowCredentials(true);// 프론트서버에서 credentials 설정 해주면 여기도 무조건 true 설정 해줘야 함
                                configuration.setAllowedHeaders(Collections.singletonList("*"));// 허용할 헤더 설정-> Audentification, content-type등 명시적으로 허용하기
                                configuration.setMaxAge(3600L);// 허용 시간 설정 -> 하루 정도로 변경하기
                                configuration.setExposedHeaders(Collections.singletonList("Authorization"));// 프론트 전송 시 Authorization 헤더에 JWT를 담아 보낼 것이기 때문에 허요

                                return configuration;
                            }
                        })
                        );


        // csrf disable
        http
                .csrf((auth) -> auth.disable());//jwt 방식은 stateless 상태로 관리하기 때문에 csrf공격을 방어하지 않아도 되므로 disable로 설정

        //폼 로그인 disable
        http
                .formLogin((auth) -> auth.disable());//Form 기반 로그인 방식(사용자가 로그인 폼을 제출하면, 서버는 해당 정보를 받아 세션을 생성하고 이증 관리) disable

        http//HTTP Basic인증방식 비활성화
                // (클라이언트 요청 시 사용자 이름과 비밀번호를 인코딩하여 서버에 전달하는 방식, 보안이 취약하며 사용자의 자격 증명을 매번 전송해야해서 JWT방식과 맞지 않음
                .httpBasic((auth)-> auth.disable());

        // 요청 경로별 권한 설정(로그인 구현 완료 후 재설정 해 줄 예정)
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/login/**", "/", "/api/register", "/api/reissue").permitAll() // 적어준 경로에 대해서는 전체 허용
                        .requestMatchers("/api/admin").hasRole("ADMIN") // 적어준 경로에는 ADMIN만 접근 가능
                        .requestMatchers("/api/reissue").permitAll() // access 토큰 만료된 상태로 요청하므로 permit all
                        .anyRequest().authenticated() // 나머지 요청에 대해서는 로그인 한 사용자들만 접근 가능함
                )
                .formLogin((formLogin) -> formLogin
                        .loginProcessingUrl("/api/login") // 로그인 경로 변경
                        .usernameParameter("username")    // 기본 username 필드명
                        .passwordParameter("password")    // 기본 password 필드명
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/api/logout") // 로그아웃 경로 변경
                        .logoutSuccessUrl("/")    // 로그아웃 성공 후 리다이렉트 경로
                        .permitAll()
                )
                .securityMatcher("/**") // Spring Security가 적용될 경로 범위 설정

        ;


        // 요청 경로별 권한 설정 (모든 요청 허용) -> 개발 중에만 허용하고 추후에 로그인 완료 되면 제거 예정
        //http.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

        // JWTFilter를 LoginFilter 앞에 달아줌
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        // 커스텀 필터 추가, loginFilter를 통해서 검증 하고, 검증 성공하면 jwtUtil을 통해서 jwt를 생성 후 다시 loginFilter에 구현된 로그인 성공 후 실행될 메소드에 jwt 반환해준다.
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);


        // 세션 stateless 상태로 관리
        http
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        );

    return http.build();
    }


}
