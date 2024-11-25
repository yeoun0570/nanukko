package nanukko.nanukko_back.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
/*시크릿 키를 가지고 JWT에 관해 발급과 검증을 담당할 클래스 - 사용자 정보 검증하는 메소드와 jwt 발급하는 메소드로 이루어짐*/
public class JWTUtil {

    private SecretKey secretKey;
    public JWTUtil(@Value("${spring.jwt.secret}") String secret){
        //jwt에서 키는 객체 타입으로 저장해둬야 함, 따라서 SecretKey 타입으로 properties 파일에 저장해놓은 키를 객체에 저장.
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /*검증을 할 메소드들 -> 토큰을 jsonParser로 꺼내서 내부 데이터 확인  */

    // username 일치하는지 확인
    public String getUsername(String token){//username이라고 되어있지만 아이디를 의미
        return Jwts.parser().verifyWith(secretKey)//secretkey를 사용해서 토큰이 우리 서버에서 생성되었는지, secretKey와 일치하는지 검사
                .build().parseSignedClaims(token)//클레임 확인 후에
                .getPayload().get("userId", String.class);//페이로드 부분에서 userId라는 키값의 String타입 데이터를 가져옴
    }

    // roll 학인
    public String getRole(String token){
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("role", String.class);
    }

    // 카테고리 확인
    public String getCategory(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    // email 확인
    public String getEmail(String token){
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("email", String.class);
    }

    // 이름 확인
    public String getNickname(String token){
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("nickname", String.class);
    }


    // 토큰 만료 확인
    public boolean isExpired(String token){
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().getExpiration().before(new Date());//페이로드에서 토큰 만료일이 new Date() 기준 만료인지 여부를 리턴
    }


    /*로그인이 성공적으로 됐을 때 successfulHandler를 통해서 전달받은 username 등의 정보를 기반으로 토큰 생성 메소드*/
    public String createJwt(String category, String userId, String nickname, String email, String role, Long expiredMs){//사용자 아이디, 사용자 이름, 권한, 만료기간
        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)//클레임 선언 후 특정한 키에 대한 값을 페이로드 부분에 넣어줄 수 있다
                .claim("nickname", nickname)// 닉네임 아니고 실제 사용자 이름
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))//토큰이 발행된 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs))//만료 시간 설정
                .signWith(secretKey, Jwts.SIG.HS256)// HS256 알고리즘으로 서명
                .compact();//토큰을 컴팩트 시킴
    }
}
