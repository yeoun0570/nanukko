package nanukko.nanukko_back.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.user.Role;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*로그인 성공 후 발급된 jwt를 검증(권한 등 검증) 할 jwt filter 클래스*/
@Log4j2
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");// request에서 Authorization 헤더 찾음

        if(authorization == null || !authorization.startsWith("Bearer ")){// 알맞은 접두사로 시작하는 권한이 있나?
            filterChain.doFilter(request, response);//필터체인에 여러 필터가 있는데 그 필터를 종료하고, 다음 필터에 req, resp 넘겨줌
            return;// authorization헤더 비어있거나 Barer가 아니면 종료
        }

        /*토큰 만료시간 검증*/
        //토큰에서 Bearer 접두사 분리
        String token = authorization.split(" ")[1];// 공백 기준으로 분리(split하면 list 생성됨) 후 list 생성됨, 뒤에 있는 실제 토큰만 꺼냄, 그 중 인덱스 1
        if(jwtUtil.isExpired(token)){
            log.info("토큰 만료~~");
            filterChain.doFilter(request, response);
            return;
        }

        // token 에서 userId와, role 획득
        String userId = jwtUtil.getUsername(token);
        Role role = Role.valueOf(jwtUtil.getRole(token));//String타입을 enum타입으로 변환

        User user = User.builder()// not null인 값들 많았어서 에러 발생할 것 같음
                .userId(userId)
                .role(role)
                .password("tempPassword")// 설정 안 해줘도 됨, 실제 비번은 dB에 없다. 매 요청마다 db 접근해서 비번 검사하면 너무 비효율적임
                .build();

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // ㅅ프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 모든 필요한 작업들이 완료되었으므로 필터체인의 그 다음 필터에서 req, resp 넘겨줌
        filterChain.doFilter(request, response);

    }
}
