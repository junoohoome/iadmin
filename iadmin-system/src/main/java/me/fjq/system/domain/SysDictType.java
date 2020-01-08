package me.fjq.system.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import me.fjq.core.BaseEntity;


/**
 * 字典类型表 sys_dict_type
 */
@Data
public class SysDictType extends BaseEntity {

    /** 字典主键 */
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    /** 字典名称 */
    private String dictName;

    /** 字典类型 */
    private String dictType;

    /** 状态（0正常 1停用） */
    private String status;

}
