package me.fjq.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

/**
 * 分页工具类
 * 提供分页安全校验，防止深度分页导致的性能问题
 *
 * @author fjq
 */
@Slf4j
public class PageUtils {

    /**
     * 默认页码
     */
    public static final long DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final long DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小（防止一次查询过多数据）
     */
    public static final long MAX_PAGE_SIZE = 100;

    /**
     * 最大分页深度（防止深度分页）
     * 超过此深度的分页请求将被限制
     */
    public static final long MAX_PAGE_DEPTH = 1000;

    private PageUtils() {
    }

    /**
     * 创建安全的分页对象
     * 自动校验并修正分页参数，防止异常值
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 安全的分页对象
     */
    public static <T> Page<T> safePage(Long pageNum, Long pageSize) {
        return safePage(pageNum, pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 创建安全的分页对象（可指定最大每页大小）
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param maxPageSize 最大每页大小限制
     * @param <T>         数据类型
     * @return 安全的分页对象
     */
    public static <T> Page<T> safePage(Long pageNum, Long pageSize, long maxPageSize) {
        // 处理 null 值
        long pn = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        long ps = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        // 限制每页最大数量
        if (ps > maxPageSize) {
            log.warn("分页大小超过限制: {} > {}, 已自动调整", ps, maxPageSize);
            ps = maxPageSize;
        }

        // 限制最大分页深度
        long maxPageNum = MAX_PAGE_DEPTH / ps;
        if (pn > maxPageNum) {
            log.warn("分页深度超过限制: {} > {}, 已自动调整", pn, maxPageNum);
            pn = maxPageNum;
        }

        return new Page<>(pn, ps);
    }

    /**
     * 从现有 Page 对象中获取安全的分页参数
     * 用于修正前端传入的分页参数
     *
     * @param page 原始分页对象
     * @param <T>  数据类型
     * @return 安全的分页对象
     */
    public static <T> Page<T> sanitize(Page<T> page) {
        if (page == null) {
            return new Page<>(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
        }
        return safePage(page.getCurrent(), page.getSize());
    }

    /**
     * 计算游标分页的偏移量
     * 适用于基于 ID 的游标分页，避免 OFFSET 性能问题
     *
     * @param lastId   上一页最后一条记录的 ID（0 表示从头开始）
     * @param pageSize 每页大小
     * @return 游标分页起始 ID
     */
    public static Long cursorStart(Long lastId, int pageSize) {
        if (lastId == null || lastId < 0) {
            return 0L;
        }
        return lastId;
    }

    /**
     * 检查分页参数是否合理
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return true 表示分页参数合理，false 表示需要调整
     */
    public static boolean isValid(Long pageNum, Long pageSize) {
        if (pageNum == null || pageNum < 1) {
            return false;
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return false;
        }
        // 检查分页深度
        long offset = (pageNum - 1) * pageSize;
        return offset <= MAX_PAGE_DEPTH;
    }

    /**
     * 获取分页统计信息（用于日志记录）
     *
     * @param page 分页对象
     * @return 分页信息字符串
     */
    public static String pageInfo(Page<?> page) {
        if (page == null) {
            return "Page{empty}";
        }
        return String.format("Page{current=%d, size=%d, total=%d, pages=%d}",
                page.getCurrent(), page.getSize(), page.getTotal(), page.getPages());
    }
}
