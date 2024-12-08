package nanukko.nanukko_back.repository.log;

import nanukko.nanukko_back.domain.log.UserActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    @Query("SELECT log.product.productId " +
            "FROM UserActionLog log " +
            "WHERE log.ageGroup = :ageGroup " +
            "AND log.product IS NOT NULL " +
            "GROUP BY log.product.productId " +
            "ORDER BY COUNT(log.product.productId) DESC")
    Page<Long> findPopularProductIdsByAgeGroup(@Param("ageGroup") int ageGroup, Pageable pageable);

    boolean existsBy();
}
