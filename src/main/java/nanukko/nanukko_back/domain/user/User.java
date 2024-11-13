package nanukko.nanukko_back.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class User {
    @Id
    @NotNull
    @Column(name = "user_id")
    private String userId; //사용자 아이디

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role; //사용자 권한
    
    private String nickname; //사용자 이름

    @Column(name = "social_login")
    @NotNull
    private boolean socialLogin; //소셜로그인 여부

    @NotNull
    private String password; //비밀번호

    @NotNull
    private String mobile; //전화번호

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt; //가입 날짜

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //사용자 수정 날짜

    @NotNull
    private boolean gender; //성별 -> true = 남자, false = 여자

    private int point; // 포인트

    // private Level level; //등급인데 쓸거임?

    @Column(name = "b_date")
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate userBirth; //생년월일

    @NotNull
    private String email; //이메일

    @Embedded
    @NotNull
    private UserAddress address; // 주소 모음

    @NotNull
    @Column(name = "review_rate")
    @ColumnDefault("50")
    @Min(0)
    @Max(100)
    private double reviewRate; //신뢰도 점수

    @NotNull
    private String status; //사용자 상태

    @NotNull
    @Column(name = "is_canceled")
    private boolean isCanceled; //삭제여부 -> true = 삭제, false = 삭제X

    @Embedded
    private Consent consent; //약관 동의 모음

    private String profile; //프로필 사진

    @NotNull
    private int balance; //계좌자금(결제 테스트용)

    //자금 추가
    public void addBalance(int amount) {
        this.balance += amount;
    }

    //자금 차감
    public void subtractBalance(int amount) {
        if(this.balance < amount) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    //사용자 정보를 수정하기 위한 메서드
    public void updateUserInfo(
            String nickname,
            String mobile,
            String email,
            UserAddress address,
            String profile
    ) {
        this.nickname = nickname;
        this.mobile = mobile;
        this.email = email;
        this.address = address;
        this.profile = profile;
        this.updatedAt = LocalDateTime.now();
    }

    //상점 평점 평균 구하기
    public void updateReviewRate(double reviewRate) {
        this.reviewRate = reviewRate;
    }
}
