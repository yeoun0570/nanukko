package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.KidId;
import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KidRepository extends JpaRepository<Kid, KidId> {
    //자녀 조회를 위한 메서드
    List<Kid> findByUserOrderByKidId(User user);
    //자녀 KidId 추가를 위한 메서드
    Optional<Kid> findByUserAndKidId(User user, String kidId);
}
