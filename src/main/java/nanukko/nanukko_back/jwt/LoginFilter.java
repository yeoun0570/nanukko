package nanukko.nanukko_back.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    // spring security에서는 불변객체 관리(보안, 안정성 관리) + 확실한 의존성 주입을 위해 생성자 주입 방식을 권장함
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String userId = obtainUsername(request);//입력값 가로채서 받기
        String password = obtainPassword(request);

        // Authentication Manager에게 id, pw 넘겨주어 검증하기 위해서는 token(여기서는 dto 역할)에 담아서 넘겨줌, roll값은 일단 null 처리
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, password, null);

        return authenticationManager.authenticate(authToken);// 만들어준 토큰 넘겨주면 자동으로 DB에서 회원 정보 들고와서 service에서 검증함
    }

    /*로그인 성공하면 실행될 메소드, 성공 후 JWTUtil에서 JWT 반환을 여기서 받음 */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();//user 객체 알아냄
        String userId = details.getUsername();
        String nickname = details.getUserNickname();// 닉네임 아니고 실제 이름ㅎ

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();// role 값(컬렉션 형태) 추출
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();// 반복자 통해서 role 값 꺼냄
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(userId, nickname, role, 60*60*10L);// 10시간 만료 설정
        response.addHeader("Authorization", "Bearer "+token);// 토큰 키 값, JWT 인증방식(공백 한 칸 필수!!), token


    }

    /*로그인 실패하면 실행될 메소드 -> 실패하면 로그인 페이지 이동*/
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 실패 메시지를 JSON으로 전달
        response.getWriter().write("{\"message\": \"로그인에 실패하였습니다. 다시 시도해주세요.\"}");
    }
}
