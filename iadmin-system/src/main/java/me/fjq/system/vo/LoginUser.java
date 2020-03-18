package me.fjq.system.vo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 登录用户实体类
 */
@Getter
@Setter
public class LoginUser {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String code;

    private String uuid;

}
