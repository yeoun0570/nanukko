package nanukko.nanukko_back.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.repository.KidRepository;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KidInfoDTO {
    private String kidId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate kidBirth;
    private boolean kidGender;

    // entity -> dto 변환
    public static KidInfoDTO from(Kid kid) {
        return KidInfoDTO.builder()
                .kidId(kid.getKidId())
                .kidBirth(kid.getKidBirth())
                .kidGender(kid.isKidGender())
                .build();
    }

    // dto -> entity
    // KidId 생성 규칙을 정의하는 정적 메서드
    public static String generateKidIdPattern(String userId, int sequence) {
        return String.format("%s_KID_%d", userId, sequence);
    }

    // 실제 kidId 생성
    public void generateKidId(User user, KidRepository kidRepository){
        List<Kid> existingKids = kidRepository.findByUserOrderByKidId(user);
        int nextNum = existingKids.size()+1;
        this.kidId = generateKidIdPattern(user.getUserId(), nextNum);
    }

    public Kid toEntity(User user, KidRepository kidRepository) {
        if(this.kidId == null){
            generateKidId(user, kidRepository);
        }
        return Kid.builder()
                .kidId(this.kidId)
                .user(user)
                .kidBirth(this.kidBirth)
                .kidGender(this.kidGender)
                .build();
    }
}
