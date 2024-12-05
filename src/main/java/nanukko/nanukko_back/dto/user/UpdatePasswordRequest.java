package nanukko.nanukko_back.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordRequest {

    @NotNull
    private String newPassword;    // 새 비밀번호
    @NotNull
    private String token; //비밀번호 재설정 토큰

    private String userId;         // 사용자 ID
    private String oldPassword;    // 기존 비밀번호
}
