package nanukko.nanukko_back.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

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
}
