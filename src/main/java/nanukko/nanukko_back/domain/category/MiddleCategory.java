package nanukko.nanukko_back.domain.category;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class MiddleCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "middle_id")
    private Long middleId; //중분류 카테고리 ID

    @ManyToOne
    @JoinColumn(name = "major_id")
    private MajorCategory major; //대분류 카테고리 ID (FK)

    @NotNull
    @Column(name = "middle_name")
    private String middleName; //중분류 카테고리명
}
