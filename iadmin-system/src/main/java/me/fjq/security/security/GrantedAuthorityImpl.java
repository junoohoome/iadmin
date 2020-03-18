package me.fjq.security.security;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

/**
 * 权限封装
 * @author fjq
 * @date 2020/03/18
 */
@Setter
public class GrantedAuthorityImpl implements GrantedAuthority {
	
	private static final long serialVersionUID = 1L;

	private String authority;

    public GrantedAuthorityImpl(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

}