package me.fjq.quartz.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * Cron 表达式工具类
 *
 * @author fjq
 */
public class CronUtils {

    private CronUtils() {
    }

    /**
     * 验证 Cron 表达式是否有效
     *
     * @param cronExpression Cron 表达式
     * @return 是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 获取下一次执行时间
     *
     * @param cronExpression Cron 表达式
     * @return 下次执行时间
     * @throws ParseException 解析异常
     */
    public static Date getNextExecution(String cronExpression) throws ParseException {
        CronExpression cron = new CronExpression(cronExpression);
        return cron.getNextValidTimeAfter(new Date());
    }

    /**
     * 通过表达式获取最近的有效时间戳
     *
     * @param cronExpression Cron 表达式
     * @return 时间戳
     * @throws ParseException 解析异常
     */
    public static long getNextValidTime(String cronExpression) throws ParseException {
        Date nextTime = getNextExecution(cronExpression);
        return nextTime != null ? nextTime.getTime() : 0L;
    }
}
