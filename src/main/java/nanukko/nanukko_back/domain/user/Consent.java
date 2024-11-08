package nanukko.nanukko_back.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Consent {

    @Column(name = "cookie_consent")
    private boolean cookieConsent; //쿠키 동의여부 -> true = 동의, false = 미동의

    @Column(name = "tos_consent")
    private boolean tosConsent; //서비스 약관 동의 여부 -> ture = 동의, false = 미동의

    @Column(name = "loc_consent")
    private boolean locConsent; //위치 동의 여부 -> ture = 동의, false = 미동의

    @Column(name = "privacy_consent")
    private boolean privacyConsent; //개인정보 처리방침 동의여부 -> ture = 동의, false = 미동의
}
