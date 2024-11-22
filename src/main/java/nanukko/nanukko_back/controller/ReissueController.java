package nanukko.nanukko_back.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nanukko.nanukko_back.service.ReissueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReissueController {

    //쿠키 생성 메소드, refresh 토큰 만료 시간과 동일하게 설정
    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setSecure(false); //true로 변경하면 https에서만 동작함
        cookie.setPath("/");// 쿠키가 설정될 범위
        cookie.setHttpOnly(true);

        return cookie;
    }
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
            List<String> newTokens = reissueService.generateNewAccessToken(refresh);

            response.setHeader("access", newTokens.get(0));

            // 새 refresh 쿠키 추가
            Cookie refreshCookie = createCookie("refresh", newTokens.get(1));
            response.addCookie(refreshCookie);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}
