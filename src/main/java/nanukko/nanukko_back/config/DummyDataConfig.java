package nanukko.nanukko_back.config;

import nanukko.nanukko_back.dto.user.KidInfoDTO;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.service.RegisterService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyDataConfig {

    @Bean
    public CommandLineRunner createDummyData(RegisterService registerService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                        .userId("user" + i)
                        .nickname("이름" + i)
                        .password("11")
                        .userBirth(LocalDate.now().minusYears(20 + i))
                        .mobile("010-1234-56" + i)
                        .email("user" + i + "@example.com")
                        .gender(i % 2 == 0)
                        .addrMain("Main Street " + i)
                        .addrDetail("Apt " + (i + 1))
                        .addrZipcode("12345" + i)
                        .kids(List.of(
                                KidInfoDTO.builder()
                                        .kidBirth(LocalDate.now().minusYears(5))
                                        .kidGender(true)
                                        .build(),
                                KidInfoDTO.builder()
                                        .kidBirth(LocalDate.now().minusYears(3))
                                        .kidGender(false)
                                        .build()
                        ))
                        .cookieConsent(true)
                        .tosConsent(true)
                        .locConsent(true)
                        .privacyConsent(true)
                        .role("USER")
                        .build();

                registerService.registerUser(userInfoDTO);
            }
        };
    }
}
