package nanukko.nanukko_back.domain.jwt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshJWT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;// 로그인한 사용자, 굳이 username을 추가해주는 이유는 브라우저마다 로그인을 하면 새 토큰이 발급되기 때문이다.

    @Column(length = 1000) // JWT 토큰 길이를 고려하여 충분한 길이로 설정
    private String refresh;// 토큰
    private String expiration;// 만료 날짜
}
