package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 操作日志记录(SysOperLog)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysOperLog {

    /**日志主键*/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**模块标题*/
    private String title;
    /**业务类型（0其它 1新增 2修改 3删除）*/
    private Integer businessType;
    /**方法名称*/
    private String method;
    /**请求方式*/
    private String requestMethod;
    /**操作类别（0其它 1后台用户 2手机端用户）*/
    private Integer operatorType;
    /**操作人员*/
    private String operName;
    /**请求URL*/
    private String operUrl;
    /**主机地址*/
    private String operIp;
    /**操作地点*/
    private String operLocation;
    /**请求参数*/
    private String operParam;
    /**返回参数*/
    private String jsonResult;
    /**操作状态（0正常 1异常）*/
    private Integer status;
    /**错误消息*/
    private String errorMsg;
    /**操作时间*/
    private Date operTime;
    /**请求参数（新增，JSON格式）*/
    private String requestParam;
    /**响应结果（新增，JSON格式）*/
    private String responseResult;
    /**执行时长（新增，毫秒）*/
    private Long costTime;

}