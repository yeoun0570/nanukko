package nanukko.nanukko_back.domain.user;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class KidId implements Serializable {
    private String user; //User의 id
    private Long kidId; //Kid의 id
}
