package nanukko.nanukko_back.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

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
    private String kidId; //Kid의 고유 PK, auto_increment 불가능

    @Column(name = "k_b_date")
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate kidBirth; //자녀 생년월일

    @Column(name = "k_gender")
    @NotNull
    private boolean kidGender;

    //자녀 정보를 수정하기 위한 메서드
    public void updateInfo(LocalDate kidBirth, boolean kidGender) {
        this.kidBirth = kidBirth;
        this.kidGender = kidGender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kid kid = (Kid) o;
        return kidGender == kid.kidGender && Objects.equals(kidBirth, kid.kidBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kidBirth, kidGender);
    }
}
