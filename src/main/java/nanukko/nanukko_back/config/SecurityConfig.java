package nanukko.nanukko_back.config;

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

//        // LoginFilter의 경로를 "/api/login"으로 설정
//        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil);
//        loginFilter.setFilterProcessesUrl("/api/login");

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
//        http
//                .authorizeHttpRequests((auth) -> auth
//                        .requestMatchers("/api/login/**", "/", "/api/register", "/ws-stomp/**").permitAll()//적어준 경로에 대해서는 전체 허용
//                        .requestMatchers("/admin").hasRole("ADMIN")//적어준 경로에는 ADMIN만 접근 가능
//                        .anyRequest().authenticated()//나머지 요청에 대해서는 로그인 한 사용자들 다 접근 가능함
//                );

        // 요청 경로별 권한 설정 (모든 요청 허용) -> 개발 중에만 허용하고 추후에 로그인 완료 되면 제거 예정
        http.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

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
