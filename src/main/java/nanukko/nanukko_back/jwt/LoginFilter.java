package nanukko.nanukko_back.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nanukko.nanukko_back.domain.jwt.RefreshJWT;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.repository.RefreshJWTRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshJWTRepository refreshJWTRepository;

    // spring security에서는 불변객체 관리(보안, 안정성 관리) + 확실한 의존성 주입을 위해 생성자 주입 방식을 권장함
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshJWTRepository refreshJWTRepository){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshJWTRepository = refreshJWTRepository;

        // 로그인 처리 URL 설정
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String userId = obtainUsername(request);//입력값 가로채서 받기
        String password = obtainPassword(request);

        // Authentication Manager에게 id, pw 넘겨주어 검증하기 위해서는 token(여기서는 dto 역할)에 담아서 넘겨줌, roll값은 일단 null 처리
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, password, null);

        return authenticationManager.authenticate(authToken);// 만들어준 토큰 넘겨주면 자동으로 DB에서 회원 정보 들고와서 service에서 검증함
    }

    // 쿠키 생성하기
    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setSecure(false); //true로 변경하면 https에서만 동작함
        cookie.setPath("/");// 쿠키가 설정될 범위
        cookie.setHttpOnly(true);

        return cookie;
    }

    /*로그인 성공하면 실행될 메소드, 성공 후 JWTUtil에서 JWT 반환을 여기서 받음 */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();//user 객체 알아냄
        String nickname = details.getUserNickname();// 닉네임 아니고 실제 이름ㅎ
        String email = details.getUserEmail();

        String userId = authentication.getName();// 사용자 아이디

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();// role 값(컬렉션 형태) 추출
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();// 반복자 통해서 role 값 꺼냄
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 토큰 생성
        String access = jwtUtil.createJwt("access", userId, nickname, email, role,600000L);// 10분 만료
        String refresh = jwtUtil.createJwt("refresh", userId, nickname, email, role, 86400000L);// 토큰 10시간 만료

        // Refresh 토큰 DB 저장
        addRefreshEntity(userId, refresh, 86400000L);

        // 응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());

    }

    /*refresh 토큰 DB 저장 메소드*/
    private void addRefreshEntity(String username, String refresh, Long expiredMs){
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshJWT entity = RefreshJWT.builder()
                .username(username)
                .refresh(refresh)
                .expiration(date.toString())
                .build();
        refreshJWTRepository.save(entity);
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
