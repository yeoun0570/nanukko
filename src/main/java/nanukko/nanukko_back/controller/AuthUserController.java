package nanukko.nanukko_back.controller;

import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@RequestMapping("/api/user")
@RestController
public class AuthUserController {

    // 사용자 아이디 & 권한 얻을 때 사용, 인증 정보를 처리하는 복잡한 로직이 필요한 경우 사용
    @GetMapping("/auth")
    public String getAuthenticatedUserInfo(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();//로그인 성공 후 임시 세션에서 이름 받음

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();//권한 받음
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();// 권한 저장

        return "사용자 정보 "+userId+role;

    }

    // 간단한 사용자 정보 추출 및 처리용
    @GetMapping("principal")
    public ResponseEntity<UserInfoDTO> getAuthenticatedUserInfoDetails(@AuthenticationPrincipal CustomUserDetails userDetails){

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


}
