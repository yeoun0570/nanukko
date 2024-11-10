package nanukko.nanukko_back.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Embeddable
@Getter
public class Image {

    @Column(name = "image1")
    @NotNull
    private String image1;
    @Column(name = "image2")
    private String image2;
    @Column(name = "image3")
    private String image3;
    @Column(name = "image4")
    private String image4;
    @Column(name = "image5")
    private String image5;
}
