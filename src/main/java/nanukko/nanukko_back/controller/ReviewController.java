package nanukko.nanukko_back.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.review.ReviewRegisterDTO;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final UserService userService;

    //후기 작성
    @PostMapping("/write")
    public ResponseEntity<ReviewRegisterDTO> writeReview(
            @Valid @RequestBody ReviewRegisterDTO reviewDTO)
    {
        ReviewRegisterDTO response = userService.writeReview(reviewDTO);
        return ResponseEntity.ok(response);
    }
}
