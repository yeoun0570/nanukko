package nanukko.nanukko_back.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
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

    public Image(List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            switch (i) {
                case 0 -> this.setImage1(urls.get(i));
                case 1 -> this.setImage2(urls.get(i));
                case 2 -> this.setImage3(urls.get(i));
                case 3 -> this.setImage4(urls.get(i));
                case 4 -> this.setImage5(urls.get(i));
            }
        }
    }

}
