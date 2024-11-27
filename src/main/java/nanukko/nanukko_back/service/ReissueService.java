package nanukko.nanukko_back.service;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.jwt.RefreshJWT;
import nanukko.nanukko_back.jwt.JWTUtil;
import nanukko.nanukko_back.repository.RefreshJWTRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshJWTRepository refreshJWTRepository;
    public ReissueService(JWTUtil jwtUtil, RefreshJWTRepository refreshJWTRepository){
        this.jwtUtil = jwtUtil;
        this.refreshJWTRepository = refreshJWTRepository;
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

        // 토큰이 refresh인지 확인(발급시 페이로드에 refresh 라고 명시)
        String category = jwtUtil.getCategory(refresh);
        if(!"refresh".equals(category)){
            throw new IllegalArgumentException("invalid refresh token");
        }

        // DB 저장되어 있는지 확인
        Boolean isExist = refreshJWTRepository.existsByRefresh(refresh);
        if(!isExist){
            throw new IllegalArgumentException("데이터베이스에 토큰 없음");
        }
    }

    // 새 access 토큰 발급
    public List<String> generateNewAccessToken(String refresh){
        String userId = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String email = jwtUtil.getEmail(refresh);
        String name = jwtUtil.getNickname(refresh);

        //새 토큰 생성(Refresh Rotate)
        String newAccessToken = jwtUtil.createJwt("access", userId, name, email, role,600000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", userId, name, email, role,86400000L);//refresh 토큰 페이로드에 id, 만료일만 있어도 될 듯? 추후 리팩토링하기

        //Refresh 토큰 저장 DB에 기존의 refresh토큰 삭제 후 새 refresh 토큰 저장
        refreshJWTRepository.deleteByRefresh(refresh);

        Date date = new Date(System.currentTimeMillis() + 86400000L);
        RefreshJWT entity = RefreshJWT.builder()
                .username(userId)
                .refresh(newRefreshToken)
                .expiration(date)
                .build();
        refreshJWTRepository.save(entity);


        List<String> newTokens = new ArrayList<>(2);
        newTokens.add(0, newAccessToken);
        newTokens.add(1, newRefreshToken);
        return newTokens;
    }

}