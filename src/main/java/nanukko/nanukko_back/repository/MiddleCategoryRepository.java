package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.category.MajorCategory;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiddleCategoryRepository extends JpaRepository<MiddleCategory, Long> {
    //대분류로부터 중분류 카테고리들을 조회
    List<MiddleCategory> findByMajor(MajorCategory major);
}
