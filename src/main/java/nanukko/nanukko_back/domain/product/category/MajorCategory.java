package nanukko.nanukko_back.domain.product.category;

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
    private Long majorId; // 대분류 카테고리 ID

    @NotNull
    @Column(name = "major_name")
    private String majorName; //대분류 카테고리 이름
}
