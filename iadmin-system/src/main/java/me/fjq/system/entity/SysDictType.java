package me.fjq.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import me.fjq.domain.BaseEntity;


/**
 * 字典类型表(SysDictType)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Getter
@Setter
public class SysDictType extends BaseEntity {

    /**字典主键*/
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;
    /**字典名称*/
    private String dictName;
    /**字典类型*/
    private String dictType;
    /**状态（0正常 1停用）*/
    private String status;

}