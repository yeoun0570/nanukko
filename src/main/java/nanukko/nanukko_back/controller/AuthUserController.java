package nanukko.nanukko_back.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.dto.user.UpdatePasswordRequest;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.jwt.JwtResetPassword;
import nanukko.nanukko_back.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthUserController {

    private final MailService mailService;
    private final UserRepository userRepository;
    private final JwtResetPassword jwtResetPassword;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 사용자 아이디 & 권한 얻을 때 사용, 인증 정보를 처리하는 복잡한 로직이 필요한 경우 사용
    @GetMapping("/auth")
    public String getAuthenticatedUserInfo() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();//로그인 성공 후 임시 세션에서 이름 받음

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();//권한 받음
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();// 권한 저장

        return "사용자 정보 " + userId + role;

    }

    // 간단한 사용자 정보 추출 및 처리용
    @GetMapping("principal")
    public ResponseEntity<UserInfoDTO> getAuthenticatedUserInfoDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {

        String roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));// 권한이 여러개일 경우 콤마로 연결

        UserInfoDTO userDto = UserInfoDTO.builder()
                .userId(userDetails.getUsername())// 아이디
                .nickname(userDetails.getUserNickname()) // 이름
                .email(userDetails.getUserEmail())
                .role(roles)
                .build();
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("find-id")
    public ResponseEntity<Void> findUserId(@RequestParam String email) {
        mailService.sendMailFindId(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("find-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request){
        String email = request.get("email");
        String userId = request.get("userId");

        User user = userRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new IllegalArgumentException("해당 정보와 일치하는 사용자를 찾을 수 없습니다. 이메일과 아이디를 확인해주세요."));

        //JWT 생성
        String token = jwtResetPassword.generateToken(user);
        String resetLink = "https://nanukko.store/auth/reset-password?token=" + token;

        mailService.sendMailResetPassword(email, resetLink);
        return ResponseEntity.ok().body("이메일로 비밀번호 재설정 링크가 발송되었습니다. 링크는 15분간 유효합니다.");
    }


    @PostMapping("reset-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request){

        String token = request.getToken();
        String newPassword = request.getNewPassword();

        //토큰 검증 및 사용자 아이디 추출
        String userId = jwtResetPassword.validateTokenAndGetUserId(token);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        //비밀번호 암호화 및 저장
        user.resetPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("비밀번호가 재설정 되었습니다.");
    }

}
