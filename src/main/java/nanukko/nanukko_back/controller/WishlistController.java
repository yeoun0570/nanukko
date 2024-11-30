package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @PostMapping("/{productId}")
    public ResponseEntity<?> toggleWishlist( //상품 찜하기
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //로그인 안된 회원은 찜 불가능
        }

        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId).orElseThrow();
        boolean isWished = wishlistService.toggleWishlist(productId, user);

        return ResponseEntity.ok().body(Map.of(
                "isWished", isWished,
                "message", isWished ? "상품을 찜했습니다." : "찜을 취소했습니다."
        ));
    }

    @PostMapping("/{productId}/view")
    public ResponseEntity<?> incrementViewCount(
            @PathVariable Long productId) {
        try {
            wishlistService.incrementViewCount(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
