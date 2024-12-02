package nanukko.nanukko_back.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nanukko.nanukko_back.dto.file.FileDTO;

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

    public Image(List<FileDTO> urls) {
        for (int i = 0; i < urls.size(); i++) {
            String uploadFileUrl = urls.get(i).getUploadFileUrl();
            switch (i) {
                case 0 -> this.setImage1(uploadFileUrl);
                case 1 -> this.setImage2(uploadFileUrl);
                case 2 -> this.setImage3(uploadFileUrl);
                case 3 -> this.setImage4(uploadFileUrl);
                case 4 -> this.setImage5(uploadFileUrl);
            }
        }
    }

}
