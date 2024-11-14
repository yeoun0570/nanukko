package nanukko.nanukko_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//유저 삭제를 위한 DTO
public class UserRemoveDTO {
    private String userId;
    private boolean canceled;
}
