package me.fjq.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.fjq.system.entity.SysRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.List;


/**
 * @author fjq
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
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

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 祖级列表（用于数据权限）
     */
    private String ancestors;

    /**
     * 角色列表（用于数据权限）
     */
    private List<SysRole> roles;

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
        return "0".equals(status);
    }

}
