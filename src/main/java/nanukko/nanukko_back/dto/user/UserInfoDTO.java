package nanukko.nanukko_back.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import nanukko.nanukko_back.domain.user.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
//마이페이지에서 내 정보 조회할 때 사용할 DTO
public class UserInfoDTO {
    private String userId;
    private String nickname;
    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate userBirth;
    private String mobile;
    private String email;
    private boolean gender;
    private String addrMain;
    private String addrDetail;
    private String addrZipcode;
    private double reviewRate;
    private String profile;
    private List<KidInfoDTO> kids;

    private boolean cookieConsent; //쿠키 동의여부 -> true = 동의, false = 미동의
    private boolean tosConsent; //서비스 약관 동의 여부 -> ture = 동의, false = 미동의
    private boolean locConsent; //위치 동의 여부 -> ture = 동의, false = 미동의
    private boolean privacyConsent; //개인정보 처리방침 동의여부

    private String role;

    // User -> UserInfoDTO 변환 메서드
    public static UserInfoDTO from(User user, List<Kid> kidList) {
        // Kid 리스트를 KidInfoDTO 리스트로 변환
        List<KidInfoDTO> kidInfoDTOS = kidList.stream()
                .map(KidInfoDTO::from) // Kid -> KidInfoDTO 변환
                .collect(Collectors.toList());

        // User -> UserInfoDTO 변환
        return UserInfoDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .userBirth(user.getUserBirth())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .gender(user.isGender())
                .addrMain(user.getAddress().getAddrMain())
                .addrDetail(user.getAddress().getAddrDetail())
                .addrZipcode(user.getAddress().getAddrZipcode())
                .reviewRate(user.getReviewRate())
                .profile(user.getProfile())
                .kids(kidInfoDTOS) // 변환된 KidInfoDTO 리스트 추가
                .cookieConsent(user.getConsent().isCookieConsent())
                .tosConsent(user.getConsent().isTosConsent())
                .locConsent(user.getConsent().isLocConsent())
                .privacyConsent(user.getConsent().isPrivacyConsent())
                .role(user.getRole().name())
                .build();
    }

    // DTO -> User Entity 변환 메서드
    public User toEntity() {
        // UserAddress 생성
        UserAddress userAddress = UserAddress.builder()
                .addrMain(this.addrMain)
                .addrDetail(this.addrDetail)
                .addrZipcode(this.addrZipcode)
                .build();

        Consent consent = Consent.builder()
                .cookieConsent(this.cookieConsent)
                .locConsent(this.locConsent)
                .tosConsent(this.tosConsent)
                .privacyConsent(this.privacyConsent)
                .build();

        // User 생성
        return User.builder()
                .userId(this.userId)
                .nickname(this.nickname)
                .password(this.password)
                .userBirth(this.userBirth)
                .mobile(this.mobile)
                .email(this.email)
                .gender(this.gender)
                .address(userAddress)
                .reviewRate(this.reviewRate)
                .profile(this.profile)
                .createdAt(LocalDateTime.now())
                .consent(consent)
                .role(Role.ROLE_USER)//회원가입하면 무조건 일반회원
                .build();
    }
}
