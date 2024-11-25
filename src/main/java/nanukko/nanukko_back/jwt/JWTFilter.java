package nanukko.nanukko_back.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.user.Role;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.service.ReissueService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/*로그인 성공 후 발급된 jwt를 검증(권한 등 검증) 할 jwt filter 클래스*/
@Log4j2
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;

    public JWTFilter(JWTUtil jwtUtil, ReissueService reissueService){
        this.jwtUtil = jwtUtil;
        this.reissueService = reissueService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");

        // Bearer  토큰이 없으면 다음 필터로
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);// 헤더에서 access키에 담긴 토큰 꺼냄

        try{
            // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
            jwtUtil.isExpired(accessToken);

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

        }catch (ExpiredJwtException e){//access 토큰 만료
            /**access 토큰 만료되면 refresh 토큰으로 검증 후 access 토큰 자동 재발급*/
            try{
                // ReissueService를 통한 토큰 재발급 시도
                String refreshToken = reissueService.getRefreshTokenFromCookies(request.getCookies());
                if (refreshToken != null) {
                    // refresh 토큰 검증
                    reissueService.validateRefreshToken(refreshToken);

                    // 새 토큰 발급
                    List<String> newTokens = reissueService.generateNewAccessToken(refreshToken);
                    String newAccessToken = newTokens.get(0);
                    String newRefreshToken = newTokens.get(1);

                    // 응답 헤더에 새 access token 설정
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    // 새 refresh token을 쿠키에 설정
                    Cookie refreshCookie = new Cookie("refresh", newRefreshToken);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setPath("/");
                    refreshCookie.setMaxAge(24 * 60 * 60); // 24시간
                    response.addCookie(refreshCookie);

                    // 새 토큰으로 인증 처리
                    String username = jwtUtil.getUsername(newAccessToken);
                    String role = jwtUtil.getRole(newAccessToken);

                    User user = User.builder()
                            .userId(username)
                            .role(Role.valueOf(role))
                            .build();

                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    Authentication authToken = new UsernamePasswordAuthenticationToken(
                            customUserDetails, null, customUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // 원래 요청 계속 진행
                    filterChain.doFilter(request, response);
                    return;
                }

            }catch (Exception err){
                // refresh 토큰 검증/재발급 실패하면..
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);//401
                log.error("토큰 재발급 실패함: "+err);
                response.getWriter().write("토큰 재발급 실패");
                return;
            }

            // refresh 토큰이 없거나 유효하지 않은 경우
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.error("refresh 토큰이 없거나 유효하지 않습니다.");
            response.getWriter().write("예상치 못한 에러가 발생했습니다. 다시 로그인 해주세요.");

        }catch (MalformedJwtException e){// 형식이 잘못된 JWT로 인한 예외
            PrintWriter writer = response.getWriter();
            writer.print("잘못된 형식의 토큰");
        }

    }
}
