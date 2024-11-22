package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.review.Review;
import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 판매자의 상점 후기 평균 구하기
    @Query("SELECT COALESCE(AVG(r.rate), 50.0) FROM Review r WHERE r.product.seller = :seller")
    double averageRateByProductSeller(@Param("seller") User seller);

    //내 상점에서 후기 조회
    Page<Review> findByProductSellerOrderByCreatedAtDesc(User user, Pageable pageable);

    // 상품으로 후기 존재하는지 체크
    boolean existsByProduct(Product product);
}
