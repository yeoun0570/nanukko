package nanukko.nanukko_back.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.repository.RefreshJWTRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableScheduling
@Log4j2
/**refresh 토큰 TTL을 위한 클래스**/
public class TokenCleanupService {

    private final RefreshJWTRepository refreshJWTRepository;

    public TokenCleanupService(RefreshJWTRepository refreshJWTRepository){
        this.refreshJWTRepository = refreshJWTRepository;
    }
    @PersistenceContext
    private EntityManager entityManager;

    // 매일 자정에 만료된 토큰들을 자동으로 삭제하는 메소드
    @Scheduled(cron = "0 0 0 * * ?")//매일 자정에 실행
    @Transactional
    public void cleanupExpiredTokens(){
        try{
            Date now = new Date();

            // JPQL 사용해서 만료된 토큰들 삭제 후 삭제된 토큰 개수 확인
            int deletedCount = entityManager.createQuery(
                    "DELETE FROM RefreshJWT r WHERE r.expiration < :now ")
                    .setParameter("now", now)
                    .executeUpdate();

            log.info("만료된 리프레시 토큰 {} 개가 삭제되었습니다.", deletedCount);
        }catch (Exception e){
            log.error("토큰 정리 중 오류 발생: "+e);
        }
    }

    // 토큰 정리
    public void cleanupExpiredTokensEmmediately(){
        try {
            Date now = new Date();

            int deletedCount = entityManager.createQuery(
                    "DELETE FROM RefreshJWT  r WHERE r.expiration < :now")
                    .setParameter("now", now)
                    .executeUpdate();

            log.info("즉시 정리: 만료된 리프레시 토큰 {} 개가 삭제되었습니다.", deletedCount);
        } catch (Exception e) {
            log.error("즉시 토큰 정리 중 오류 발생: ", e);
        }
    }
}
