package nanukko.nanukko_back.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Embeddable
@Getter
@Setter
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

    public Image createImageFromUrls(List<String> urls) {
        Image image = new Image();

        for (int i = 0; i < urls.size(); i++) {
            switch (i) {
                case 0 -> image.setImage1(urls.get(i));
                case 1 -> image.setImage2(urls.get(i));
                case 2 -> image.setImage3(urls.get(i));
                case 3 -> image.setImage4(urls.get(i));
                case 4 -> image.setImage5(urls.get(i));
            }
        }

        return image;
    }

}
