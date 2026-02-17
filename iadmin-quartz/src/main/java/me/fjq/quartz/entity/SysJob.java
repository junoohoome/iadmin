package me.fjq.quartz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import me.fjq.domain.BaseEntity;

/**
 * 定时任务调度实体类
 *
 * @author fjq
 */
@Getter
@Setter
@TableName("sys_job")
public class SysJob extends BaseEntity {

    /**
     * 任务ID
     */
    @TableId(value = "job_id", type = IdType.AUTO)
    private Long jobId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    private String cronExpression;

    /**
     * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
     */
    private String misfirePolicy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    private String concurrent;

    /**
     * 状态（0正常 1暂停）
     */
    private String status;

    /**
     * 下次执行时间（非数据库字段）
     */
    @TableField(exist = false)
    private Long nextValidTime;

    /**
     * 常量定义
     */
    public static final String STATUS_NORMAL = "0";
    public static final String STATUS_PAUSE = "1";
    public static final String CONCURRENT_ALLOW = "0";
    public static final String CONCURRENT_DISALLOW = "1";
    public static final String MISFIRE_RUN_NOW = "1";
    public static final String MISFIRE_IGNORE_MISFIRES = "2";
    public static final String MISFIRE_DO_NOTHING = "3";
}
