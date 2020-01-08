package me.fjq.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import me.fjq.core.BaseEntity;


/**
 * 通知公告表 sys_notice
 */
@Data
public class SysNotice extends BaseEntity {

    /** 公告ID */
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1通知 2公告） */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态（0正常 1关闭） */
    private String status;

}
