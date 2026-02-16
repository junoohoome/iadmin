package me.fjq.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.fjq.annotation.Log;
import me.fjq.system.entity.SysOperLog;
import me.fjq.system.service.SysOperLogService;
import me.fjq.utils.IpUtils;
import me.fjq.utils.SystemSecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志记录切面
 *
 * <p>拦截所有标注了 @Log 注解的 Controller 方法，自动记录操作日志
 *
 * @author fjq
 * @since 2025-02-16
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    private final SysOperLogService operLogService;

    public LogAspect(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    /**
     * 配置切入点：所有标注了 @Log 注解的方法
     */
    @Pointcut("@annotation(me.fjq.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 方法正常返回后处理
     *
     * @param joinPoint 切点
     * @param result    返回值
     * @param logAnnotation 日志注解
     */
    @AfterReturning(pointcut = "logPointcut() && @annotation(logAnnotation)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result, Log logAnnotation) {
        handleLog(joinPoint, logAnnotation, null, result);
    }

    /**
     * 方法抛出异常后处理
     *
     * @param joinPoint 切点
     * @param e         异常
     * @param logAnnotation 日志注解
     */
    @AfterThrowing(value = "logPointcut() && @annotation(logAnnotation)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e, Log logAnnotation) {
        handleLog(joinPoint, logAnnotation, e, null);
    }

    /**
     * 处理日志记录
     *
     * @param joinPoint 切点
     * @param logAnnotation 日志注解
     * @param e         异常（可为空）
     * @param result    返回结果（可为空）
     */
    private void handleLog(JoinPoint joinPoint, Log logAnnotation, Exception e, Object result) {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 构建操作日志实体
            SysOperLog operLog = new SysOperLog();

            // 设置基础信息
            operLog.setStatus(0); // 默认成功
            operLog.setOperIp(IpUtils.getIp(request));
            operLog.setOperUrl(StrUtil.sub(request.getRequestURI(), 0, 255));
            operLog.setOperTime(new Date());
            operLog.setRequestMethod(request.getMethod());

            // 设置模块信息
            operLog.setTitle(logAnnotation.title());
            operLog.setBusinessType(logAnnotation.businessType());
            operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
            operLog.setOperatorType(0); // 后台用户

            // 设置操作人员
            try {
                String username = SystemSecurityUtils.getCurrentUsername();
                operLog.setOperName(StrUtil.isNotBlank(username) ? username : "未知");
            } catch (Exception ex) {
                operLog.setOperName("未知");
            }

            // 设置请求参数
            if (logAnnotation.isSaveRequestData() || logAnnotation.saveRequestParamToDb()) {
                setRequestValue(joinPoint, operLog, logAnnotation);
            }

            // 处理异常情况
            if (e != null) {
                operLog.setStatus(1); // 异常
                operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            }

            // 设置响应结果
            if (logAnnotation.isSaveResponseData() || logAnnotation.saveResponseResultToDb()) {
                if (result != null) {
                    operLog.setResponseResult(StrUtil.sub(JSONUtil.toJsonStr(result), 0, 2000));
                }
            }

            // 异步保存日志
            final SysOperLog finalOperLog = operLog;
            CompletableFuture.runAsync(() -> {
                try {
                    operLogService.save(finalOperLog);
                    log.debug("操作日志记录成功: {}", finalOperLog.getTitle());
                } catch (Exception ex) {
                    log.error("操作日志保存失败", ex);
                }
            });

        } catch (Exception ex) {
            log.error("操作日志记录异常", ex);
        }
    }

    /**
     * 获取请求参数
     *
     * @param joinPoint 切点
     * @param operLog   操作日志
     * @param logAnnotation 日志注解
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, Log logAnnotation) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }

            Map<String, Object> params = new HashMap<>();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                String paramName = "arg" + i;

                // 过滤不需要记录的参数类型
                if (shouldIgnore(arg)) {
                    continue;
                }

                params.put(paramName, arg);
            }

            String paramsJson = JSONUtil.toJsonStr(params);

            // 保存到 operParam 字段（兼容旧版本）
            if (logAnnotation.isSaveRequestData()) {
                operLog.setOperParam(StrUtil.sub(paramsJson, 0, 2000));
            }

            // 保存到 requestParam 字段（新版本）
            if (logAnnotation.saveRequestParamToDb()) {
                operLog.setRequestParam(paramsJson);
            }

        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
        }
    }

    /**
     * 判断参数是否应该忽略
     */
    private boolean shouldIgnore(Object arg) {
        return arg == null
                || arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof MultipartFile
                || arg instanceof MultipartFile[]
                || arg instanceof BindingResult;
    }
}
