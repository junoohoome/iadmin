package me.fjq.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * 系统访问记录(Logininfor)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Getter
@Setter
@TableName("sys_logininfor")
public class Logininfor {

    /**访问ID*/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**用户账号*/
    private String userName;
    /**登录IP地址*/
    private String ipaddr;
    /**登录地点*/
    private String loginLocation;
    /**浏览器类型*/
    private String browser;
    /**操作系统*/
    private String os;
    /**登录状态（0成功 1失败）*/
    private String status;
    /**提示消息*/
    private String msg;
    /**访问时间*/
    private Date loginTime;

}
