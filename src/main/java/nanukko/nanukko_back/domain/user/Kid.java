package nanukko.nanukko_back.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@IdClass(KidId.class)
public class Kid {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id") //외래 키
    @NotNull
    private User user; //외래키의 주인

    @Id
    @NotNull
    @Column(name = "kid_id")
    private Long kidId; //Kid의 고유 PK, auto_increment 불가능

    @Column(name = "k_b_date")
    @NotNull
    private LocalDateTime kidBirth; //자녀 생년월일

    @Column(name = "k_gender")
    @NotNull
    private boolean kidGender;
}
