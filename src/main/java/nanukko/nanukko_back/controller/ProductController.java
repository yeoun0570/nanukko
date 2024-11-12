package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.user.UserSetProductDTO;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api")
public class ProductController {
    private final UserService userService;

    // 현재 이미지를 varchar(255)로 받아서 이미지 크기를 담을 수가 없어서 생성이 안됨. 나중에 클라우드 도입시키고 새로 짜야될거 같음
    @PostMapping("/product/register")
    public ResponseEntity<?> registerProduct(
            @RequestParam String userId,
            @RequestBody UserSetProductDTO productDTO
    ) {
        try {
            log.info("Received userId: {}", userId);
            log.info("Received productDTO: {}", productDTO);

            UserSetProductDTO savedProduct = userService.registerProduct(userId, productDTO);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("Error registering product", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
