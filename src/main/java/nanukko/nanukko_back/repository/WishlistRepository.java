package nanukko.nanukko_back.repository;

import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.domain.wishlist.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUser(User user, Pageable pageable);
}
