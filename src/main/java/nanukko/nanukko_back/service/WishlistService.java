package nanukko.nanukko_back.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.domain.wishlist.Wishlist;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public boolean toggleWishlist(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 이미 찜한 상품인지 확인
        Optional<Wishlist> existingWishlist = wishlistRepository
                .findByUserAndProduct(user, product);

        if (existingWishlist.isPresent()) {
            // 이미 찜한 상품이면 제거
            wishlistRepository.delete(existingWishlist.get());
            return false;
        } else {
            // 찜하지 않은 상품이면 추가
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistRepository.save(wishlist);
            return true;
        }
    }

}
