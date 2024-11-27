package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.domain.wishlist.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUser(User user, Pageable pageable);

    void deleteWishlistByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    Optional<Wishlist> findByUserAndProduct(User user, Product product);
}
