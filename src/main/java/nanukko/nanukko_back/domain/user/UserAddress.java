package nanukko.nanukko_back.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddress {

    @Column(name = "addr_main")
    @NotNull
    private String addrMain; // 주소

    @Column(name = "addr_detail")
    @NotNull
    private String addrDetail; // 상세 주소

    @Column(name = "addr_zipcode")
    @NotNull
    private String addrZipcode; // 우편번호
}
