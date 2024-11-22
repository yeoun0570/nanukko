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

    private String username;// 로그인한 사용자

    @Column(length = 1000) // JWT 토큰 길이를 고려하여 충분한 길이로 설정
    private String refresh;// 토큰
    private String expiration;// 만료 날짜
}
