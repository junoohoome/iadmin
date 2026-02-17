package me.fjq.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import me.fjq.domain.BaseEntity;


/**
 * 字典数据表(SysDictData)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Getter
@Setter
public class SysDictData extends BaseEntity {

    /**字典编码*/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**字典排序*/
    private Integer dictSort;
    /**字典标签*/
    private String dictLabel;
    /**字典键值*/
    private String dictValue;
    /**字典类型*/
    private String dictType;
    /**样式属性（其他样式扩展）*/
    private String cssClass;
    /**表格回显样式*/
    private String listClass;
    /**是否默认（Y是 N否）*/
    private String isDefault;
    /**状态（0正常 1停用）*/
    private String status;

}
