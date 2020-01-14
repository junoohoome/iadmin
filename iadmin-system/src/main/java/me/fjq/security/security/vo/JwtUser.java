package me.fjq.security.security.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 *
 */

@Getter
@AllArgsConstructor
public class JwtUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickName;

    private String sex;

    @JsonIgnore
    private String password;

    private String avatar;

    private String email;

    private String phone;


    @JsonIgnore
    private Collection<GrantedAuthority> authorities;

    private boolean enabled;

    private Date createTime;

    @JsonIgnore
    private Date lastPasswordResetDate;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

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
        return enabled;
    }


}
