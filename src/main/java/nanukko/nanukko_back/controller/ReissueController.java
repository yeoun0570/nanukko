package nanukko.nanukko_back.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nanukko.nanukko_back.service.ReissueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReissueController {
    private final ReissueService reissueService;

    public ReissueController(ReissueService reissueService){
        this.reissueService = reissueService;
    }

    // access 토큰 만료됐을 경우 refresh 토큰 이용해서 새 access 토큰 발급
    @PostMapping("/api/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
        try {
            // 쿠키에서 refresh 토큰 추출
            String refresh = reissueService.getRefreshTokenFromCookies(request.getCookies());

            // refresh 토큰 검증
            reissueService.validateRefreshToken(refresh);

            // 새 Access 토큰 생성
            String newAccess = reissueService.generateNewAccessToken(refresh);

            // 응답 헤더에 Access 토큰 설정
            response.setHeader("access", newAccess);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}
