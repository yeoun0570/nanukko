package nanukko.nanukko_back.dto.user;

import nanukko.nanukko_back.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/*Spring Security에서 인증 및 권한 부여를 위해 필요한 사용자 정보를 제공*/
public class CustomUserDetails implements UserDetails {

    private final User user;
    public CustomUserDetails(User user){
        this.user = user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();// 기본값은 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();// 기본값은 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();// 기본값은 true
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();// 기본값은 true
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {//roll반환, roll은 한 명의 사용자에 대해 여러개일 수 있음
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().name();
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 아이디
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    // 이름
    public String getUserNickname(){
        return user.getNickname();
    }

    public String getUserEmail(){
        return user.getEmail();
    }
}
