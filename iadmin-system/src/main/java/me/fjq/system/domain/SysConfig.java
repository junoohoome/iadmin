package me.fjq.system.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import me.fjq.Domain.BaseEntity;


/**
 * 参数配置表 sys_config
 */
@Data
public class SysConfig extends BaseEntity {

    /** 参数主键 */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /** 参数名称 */
    private String configName;

    /** 参数键名 */
    private String configKey;

    /** 参数键值 */
    private String configValue;

    /** 系统内置（Y是 N否） */
    private String configType;

}
