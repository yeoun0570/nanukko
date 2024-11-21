package nanukko.nanukko_back.service;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.jwt.JWTUtil;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    public ReissueService(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }
    // 쿠키에서 refresh 토큰 추출
    public String getRefreshTokenFromCookies(Cookie[] cookies){
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("refresh")){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // refresh 토큰 검사
    public void validateRefreshToken(String refresh){

        if(refresh == null){
            throw new IllegalArgumentException("refresh 토근 null");
        }

        try{
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            throw new IllegalArgumentException("refresh 토큰 만료");
        }

        String category = jwtUtil.getCategory(refresh);
        if(!"refresh".equals(category)){
            throw new IllegalArgumentException("invalid refresh token");
        }
    }

    // 새 access 토큰 발급
    public String generateNewAccessToken(String refresh){
        String userId = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String email = jwtUtil.getEmail(refresh);
        String name = jwtUtil.getNickname(refresh);

        String newAccessToken = jwtUtil.createJwt("access", userId, name, email, role,600000L);
        log.info("새 토큰"+newAccessToken);
        //Access 토큰 생성
        return newAccessToken;
    }

}