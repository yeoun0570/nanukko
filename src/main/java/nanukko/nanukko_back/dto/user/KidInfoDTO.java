package nanukko.nanukko_back.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KidInfoDTO {
    private Long kidId;
    @JsonFormat(pattern = "yyyy년 MM월 dd일")
    private LocalDateTime kidBirth;
    private boolean kidGender;
}
