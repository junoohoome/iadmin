package me.fjq.system.vo;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录用户实体类
 */
@Getter
@Setter
public class AuthUser {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String code;
    private String uuid = "";

}
