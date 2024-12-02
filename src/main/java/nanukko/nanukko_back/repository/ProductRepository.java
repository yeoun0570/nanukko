package nanukko.nanukko_back.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.product.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //마이페이지 판매 상품을 페이징처리하여 조회하기 위해 사용될 메서드
    Page<Product> findBySellerAndStatusAndIsDeletedFalseOrderByCreatedAtDesc (User seller, ProductStatus status, Pageable pageable);

    //비관적 락을 사용한 상품 조회 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    //@Lock 어노테이션은 JPA 에게 비관적 락을 사용하라고 지시
    // PESSIMISTIC_WRITE는 배타적 락으로, 다음과 같은 특징이 있습니다:
    // - 다른 트랜잭션이 해당 데이터를 읽거나 쓰는 것을 모두 차단
    // - 데이터베이스 수준에서 'SELECT FOR UPDATE' 쿼리를 실행
    // - 첫 번째 트랜잭션이 커밋되거나 롤백될 때까지 다른 트랜잭션은 대기
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "10000")})
    // 락 획득 시도 시 대기할 시간을 설정합니다.
    // 3000ms(3초) 동안 락 획득을 시도하고, 실패하면 예외를 발생시킵니다.
    // 이는 무한정 대기하는 것을 방지하기 위한 설정입니다.
    @Query("select p from Product p where p.productId = :id")
    // 실제 실행될 JPQL 쿼리를 정의합니다.
    // 일반 select 쿼리지만 @Lock 애노테이션으로 인해 FOR UPDATE가 추가됩니다.
    // 실제 실행되는 SQL은 다음과 같습니다:
    // SELECT * FROM product WHERE id = ? FOR UPDATE
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long productId);


    //상품명 조회
    @Query("SELECT p FROM Product p WHERE p.productName LIKE CONCAT('%', :query, '%') AND p.isDeleted = false")
    Page<Product> searchByName(@Param("query") String query, Pageable pageable);

    //대분류 카테고리 조회
    @Query("SELECT p FROM Product p WHERE p.middleCategory.major.majorId = :majorId AND p.isDeleted = false")
    Page<Product> findByMajorId(@Param("majorId") Long majorId, Pageable pageable);

    //중분류 카테고리 조회
    @Query("SELECT p FROM Product p WHERE p.middleCategory.middleId = :middleId AND p.isDeleted = false")
    Page<Product> findByMiddleId(@Param("middleId") Long middleId, Pageable pageable);


    //관련 상품 조회
    @Query("SELECT p FROM Product p " +
            "WHERE p.productId != :productId " +
            "AND p.middleCategory.middleId = :middleId " +
            "AND p.status = 'SELLING' " +
            "AND p.isDeleted = false " +
            "ORDER BY p.updatedAt DESC")
    List<Product> findRelatedProductsByMiddleId(
            @Param("productId") Long productId,
            @Param("middleId") Long middleId,
            Pageable pageable
    );

    default List<Product> findRelatedProducts(Long productId, Long middleId) {
        return findRelatedProductsByMiddleId(
                productId,
                middleId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
    }

    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    //사용자의 자신의 상품 개수
    int countBySeller(User user);

}
