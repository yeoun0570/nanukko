package nanukko.nanukko_back.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nanukko.nanukko_back.repository.RefreshJWTRepository;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshJWTRepository refreshJWTRepository;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshJWTRepository refreshJWTRepository){
        this.jwtUtil = jwtUtil;
        this.refreshJWTRepository = refreshJWTRepository;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain );
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException{

        //경로확인
        String requestUri = request.getRequestURI();
        if(!requestUri.matches("^\\/api\\/logout$")){
            filterChain.doFilter(request, response);
            return;
        }

        // 메소드 확인
        String requstMethod = request.getMethod();
        if(!requstMethod.equals("POST")){
            filterChain.doFilter(request, response);
            return;
        }

        // refresh 토큰 얻기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }

        //refresh 토큰 null 체크
        if(refresh == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//응답 코드 400
            return;
        }

        //만료 확인
        try {
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);// 400
            return;
        }

        // DB 저장되어 있는지 확인
        Boolean isExist = refreshJWTRepository.existsByRefresh(refresh);
        if(!isExist){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        /**로그아웃 진행**/
        // DB에서 refresh 토큰 제거
        refreshJWTRepository.deleteByRefresh(refresh);

        //쿠키에 있는 refresh 제거
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);// 200
    }
}