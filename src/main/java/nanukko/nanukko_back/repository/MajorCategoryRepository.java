package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.category.MajorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorCategoryRepository extends JpaRepository<MajorCategory, Long> {
}
