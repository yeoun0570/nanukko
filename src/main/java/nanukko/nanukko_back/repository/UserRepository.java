package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByUserIdAndIsCanceledFalse(String userId);
}
