package me.fjq.quartz.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时任务示例类
 * <p>
 * 该类中的方法可以被定时任务调度系统调用。
 * 调用格式为：demoTask.方法名(参数)
 * <p>
 * 例如：
 * - demoTask.noParams                    无参调用
 * - demoTask.params('ry')                带字符串参数
 * - demoTask.multipleParams('ry', true, 2000L, 316.50D)  多参数调用
 *
 * @author fjq
 */
@Slf4j
@Component("demoTask")
public class DemoTask {

    /**
     * 无参任务示例
     */
    public void noParams() {
        log.info("执行无参任务: demoTask.noParams()");
    }

    /**
     * 带参数任务示例
     *
     * @param params 参数
     */
    public void params(String params) {
        log.info("执行带参数任务: demoTask.params('{}')", params);
    }

    /**
     * 多参数任务示例
     *
     * @param s 字符串参数
     * @param b 布尔参数
     * @param l 长整型参数
     * @param d 双精度参数
     */
    public void multipleParams(String s, Boolean b, Long l, Double d) {
        log.info("执行多参数任务: demoTask.multipleParams('{}', {}, {}, {})", s, b, l, d);
    }
}
