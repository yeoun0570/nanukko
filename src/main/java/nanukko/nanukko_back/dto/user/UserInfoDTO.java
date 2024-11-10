package nanukko.nanukko_back.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
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
    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    private LocalDateTime userBirth;
    private String mobile;
    private String email;
    private boolean gender;
    private String addrMain;
    private String addrDetail;
    private String addrZipcode;
    private int score;
    private String profile;
    private List<KidInfoDTO> kids;
}
