package nanukko.nanukko_back.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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
import java.io.PrintWriter;

/*로그인 성공 후 발급된 jwt를 검증(권한 등 검증) 할 jwt filter 클래스*/
@Log4j2
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = request.getHeader("access");// 헤더에서 access키에 담긴 토큰 꺼냄

        if(accessToken == null){// 토큰이 없다면 다음 필터로 넘김
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try{
            jwtUtil.isExpired(accessToken);
        }catch (ExpiredJwtException e){//토큰 만료
            //response body에 에러 메시지 표시
            PrintWriter writer = response.getWriter();
            writer.print("access 토큰 만료");

            //response status code 반환
            response.setStatus((HttpServletResponse.SC_UNAUTHORIZED));
            // 프론트엔드로 보내줄 응답 코드 지정, 추후 프론트엔드와 협의하여 알맞은 코드 선택해서 보내기
            // 응답에 따라 만료됐으면 다시 refresh 토큰 응답하여 access 토큰 발행 할 수 있도록 하기
            return;
        }catch (MalformedJwtException e){// 형식이 잘못된 JWT로 인한 예외
            PrintWriter writer = response.getWriter();
            writer.print("잘못된 형식의 토큰");
        }

        // 토큰이 access인지 확인(발급시 페이로드에서 확인 가능)
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")){
            //response body 메시지
            PrintWriter writer = response.getWriter();
            writer.print("access 토큰 아님");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        // username, role 값 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        User user = User.builder()
                .userId(username)
                .role(Role.valueOf(role))
                .build();

        //user 객체를 래핑하여 spring security가 이해할 수 있는 형태로 변환
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //Authentication 인터페이스의 구현체에 principal(인증된 사용자 정보), credentials(비밀번호), 사용자의 권한 목록 넘겨줌
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        //security context에 생성한 Authentication 객체를 설정 => 요청 처리 중 인증된 사용자 정보에 접근할 수 있게 된다.(인증된 사용자 정보는 이후 요청 처리에서 활용될 수 있다.)
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //요청과 인증 과정을 통과했고, 다음 필터로 요청 전달한다.
        filterChain.doFilter(request, response);

    }
}
