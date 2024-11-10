package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/my-store")
public class UserController {
    private final UserService userService;

    //사용자가 자신의 정보 조회
    @GetMapping("/info")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            //@AuthenticationPrincipal UserDetails userDetails  // 현재 로그인한 사용자(시큐리티)
            @RequestParam String userId
    ) {
        UserInfoDTO userInfoDTO = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfoDTO);
    }

}
