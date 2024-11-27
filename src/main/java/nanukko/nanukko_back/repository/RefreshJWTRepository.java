package nanukko.nanukko_back.repository;

import jakarta.transaction.Transactional;
import nanukko.nanukko_back.domain.jwt.RefreshJWT;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshJWTRepository extends JpaRepository<RefreshJWT, Long> {
    boolean existsByRefresh(String refresh);

    @Transactional
        // 커스텀한 delete by 메소드는 @Transactional 처리를 진행하지 않으면 동작시 예외가 발생
    void deleteByRefresh(String refresh);
}
