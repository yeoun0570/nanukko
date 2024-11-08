package nanukko.nanukko_back.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserAddress {

    @Column(name = "addr_main")
    private String addrMain; // 주소

    @Column(name = "addr_detail")
    private String addrDetail; // 상세 주소

    @Column(name = "addr_zipcode")
    private String addrZipcode; // 우편번호
}
