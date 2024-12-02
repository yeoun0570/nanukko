package nanukko.nanukko_back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.review.ReviewRegisterDTO;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final UserService userService;

    //후기 작성
    @PostMapping("/write")
    public ResponseEntity<ReviewRegisterDTO> writeReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRegisterDTO reviewDTO) {
        String userId = userDetails.getUsername();
        ReviewRegisterDTO response = userService.writeReview(userId, reviewDTO);
        return ResponseEntity.ok(response);
    }

    // 이미 작성한 후기인지 체크
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkReviewExists(
            @RequestParam String orderId
    ) {
        boolean exists = userService.checkReviewExists(orderId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

}
