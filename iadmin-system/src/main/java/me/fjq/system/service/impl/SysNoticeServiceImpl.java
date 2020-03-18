package me.fjq.system.service.impl;


import me.fjq.system.mapper.SysNoticeMapper;
import me.fjq.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

/**
 * 公告 服务层实现
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    private final SysNoticeMapper noticeMapper;

    public SysNoticeServiceImpl(SysNoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }


}
