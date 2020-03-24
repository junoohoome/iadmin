package me.fjq.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;


/**
 * @author fjq
 */

@Getter
@Setter
@AllArgsConstructor
public class JwtUserDetails implements UserDetails {

    private Long id;

    private String username;

    private String nickName;

    private String sex;

    @JsonIgnore
    private String password;

    private String avatar;

    private String email;

    private String mobile;

    @JsonIgnore
    private Collection<GrantedAuthority> authorities;

    private String status;

    private Date createTime;

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return status.equals("0") ? true : false;
    }

}
