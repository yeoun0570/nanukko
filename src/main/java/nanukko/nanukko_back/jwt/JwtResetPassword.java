package nanukko.nanukko_back.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtResetPassword {

    private final UserRepository userRepository;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public JwtResetPassword(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*jwt 시크릿 키를 SecretKeySpec으로 변환 - JWT 라이브러리 0.11.x 이후*/
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /*토큰으로 클레임 생성*/
    public Claims parseToken(String token){
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }


    /*토큰 생성*/
    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ 15*60*1000))//15분 유효
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateTokenAndGetUserId(String token){
        try{
            Claims claims = parseToken(token);
            return claims.getSubject();
        }catch (JwtException e){
            return  null;//유효하지 않은 토큰일 때
        }

    }
}
