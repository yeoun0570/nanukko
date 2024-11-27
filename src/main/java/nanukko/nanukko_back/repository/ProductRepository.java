package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //마이페이지 판매 상품을 페이징처리하여 조회하기 위해 사용될 메서드
    Page<Product> findBySellerAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(User seller, ProductStatus status, Pageable pageable);

    //상품명 조회
    @Query("SELECT p FROM Product p WHERE p.productName LIKE CONCAT('%', :query, '%')")
    Page<Product> searchByName(@Param("query") String query, Pageable pageable);

    //대분류 카테고리 조회
    @Query("SELECT p FROM Product p WHERE p.middleCategory.major.majorId = :majorId")
    Page<Product> findByMajorId(@Param("majorId") Long majorId, Pageable pageable);

    //중분류 카테고리 조회
    @Query("SELECT p FROM Product p WHERE p.middleCategory.middleId = :middleId")
    Page<Product> findByMiddleId(@Param("middleId") Long middleId, Pageable pageable);


    //관련 상품 조회
    @Query("SELECT p FROM Product p " +
            "WHERE p.productId != :productId " +
            "AND p.middleCategory.middleId = :middleId " +
            "AND p.status = 'SALE' " +
            "ORDER BY p.updatedAt DESC")
    List<Product> findRelatedProductsByMiddleId(
            @Param("productId") Long productId,
            @Param("middleId") Long middleId,
            Pageable pageable);

    default List<Product> findRelatedProducts(Long productId, Long middleId) {
        return findRelatedProductsByMiddleId(
                productId,
                middleId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
    }

}
