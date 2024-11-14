package nanukko.nanukko_back.domain.blacklist;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class Blacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "black_id")
    private Long blackId; //블랙리스트 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User black; //블랙리스트 회원 ID (FK)

    @NotNull
    private String reason; //정지 사유

    @NotNull
    private int period; // 정지 기간

    @NotNull
    @Column(name = "start_at")
    private LocalDateTime startAt; //정지 시작 날짜

    @NotNull
    private int count; //정지먹은 횟수
}
