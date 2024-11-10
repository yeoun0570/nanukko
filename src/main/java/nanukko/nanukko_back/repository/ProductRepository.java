package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findBySellerAndStatusOrderByCreatedAtDesc(User seller, ProductStatus status, Pageable pageable);
}
