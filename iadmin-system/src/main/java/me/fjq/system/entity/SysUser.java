package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.fjq.Domain.BaseEntity;

import java.util.Date;
import java.util.List;


/**
 * 用户信息表(SysUser)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysUser extends BaseEntity {

    /**用户ID*/
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    /**用户账号*/
    private String userName;
    /**用户昵称*/
    private String nickName;
    /**用户类型（00系统用户）*/
    private String userType;
    /**用户邮箱*/
    private String email;
    /**手机号码*/
    private String mobile;
    /**用户性别（0男 1女 2未知）*/
    private String sex;
    /**头像地址*/
    private String avatar;
    /**密码 - 仅允许写入，不允许在API响应中返回*/
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    /**帐号状态（0正常 1停用）*/
    private String status;
    /**删除标志（0代表存在 1代表删除）*/
    private String delFlag;
    /**最后登陆IP*/
    private String loginIp;
    /**最后登陆时间*/
    private Date loginDate;
    /**用户角色IDS*/
    private String roleIds;
    /**部门ID*/
    private Long deptId;
    /**祖级列表*/
    private String ancestors;
    /**用户角色名称*/
    @TableField(exist = false)
    private List<String> roleNames;

}