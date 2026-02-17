package me.fjq.quartz.job;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止并发执行的 Job
 * <p>
 * 使用 @DisallowConcurrentExecution 注解，确保同一任务在上一次执行完成前不会开始下一次执行
 *
 * @author fjq
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends QuartzJobExecution {

}
