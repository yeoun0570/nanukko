package nanukko.nanukko_back.domain.category;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class MajorCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private Long majorId; // 대분류 카테고리 ID

    @OneToMany(mappedBy = "major", fetch = FetchType.LAZY)
    private List<MiddleCategory> middleCategories; //중분류 카테고리 ID (FK)

    @NotNull
    @Column(name = "major_name")
    private String majorName; //대분류 카테고리 이름
}
