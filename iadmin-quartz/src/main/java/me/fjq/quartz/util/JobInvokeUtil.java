package me.fjq.quartz.util;

import lombok.extern.slf4j.Slf4j;
import me.fjq.quartz.entity.SysJob;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 任务执行工具类
 *
 * @author fjq
 */
@Slf4j
public class JobInvokeUtil {

    private JobInvokeUtil() {
    }

    /**
     * 执行任务
     *
     * @param sysJob 系统任务
     */
    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        if (!isValidClassName(beanName)) {
            // 通过 Spring 容器获取 Bean 并执行方法
            Object bean = SpringUtils.getBean(beanName);
            invokeMethod(bean, methodName, methodParams);
        } else {
            // 通过反射执行类的静态方法
            Object bean = Class.forName(beanName).getDeclaredConstructor().newInstance();
            invokeMethod(bean, methodName, methodParams);
        }
    }

    /**
     * 调用任务方法
     */
    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams) throws Exception {
        if (methodParams != null && !methodParams.isEmpty()) {
            Method method = findMethod(bean.getClass(), methodName, methodParams);
            if (method == null) {
                throw new RuntimeException("Method not found: " + methodName);
            }
            method.setAccessible(true);
            method.invoke(bean, getMethodParamValues(methodParams));
        } else {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(bean);
        }
    }

    /**
     * 查找匹配的方法
     */
    private static Method findMethod(Class<?> clazz, String methodName, List<Object[]> methodParams) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == methodParams.size()) {
                return method;
            }
        }
        return null;
    }

    /**
     * 获取参数值数组
     */
    private static Object[] getMethodParamValues(List<Object[]> methodParams) {
        Object[] values = new Object[methodParams.size()];
        for (int i = 0; i < methodParams.size(); i++) {
            values[i] = methodParams.get(i)[0];
        }
        return values;
    }

    /**
     * 判断是否为Class包名
     */
    public static boolean isValidClassName(String invokeTarget) {
        return StringUtils.hasText(invokeTarget) && invokeTarget.contains(".");
    }

    /**
     * 获取bean名称
     */
    public static String getBeanName(String invokeTarget) {
        String beanName = invokeTarget.substring(0, invokeTarget.indexOf("("));
        return beanName.substring(beanName.lastIndexOf(".") + 1);
    }

    /**
     * 获取bean类名（完整路径）
     */
    public static String getBeanClassName(String invokeTarget) {
        if (invokeTarget.contains("(")) {
            return invokeTarget.substring(0, invokeTarget.indexOf("("));
        }
        return invokeTarget;
    }

    /**
     * 获取方法名称
     */
    public static String getMethodName(String invokeTarget) {
        String target = invokeTarget.substring(0, invokeTarget.indexOf("("));
        return target.substring(target.lastIndexOf(".") + 1);
    }

    /**
     * 获取方法参数
     */
    public static List<Object[]> getMethodParams(String invokeTarget) {
        String methodStr = invokeTarget.substring(invokeTarget.indexOf("(") + 1, invokeTarget.indexOf(")"));
        if (!StringUtils.hasText(methodStr)) {
            return new LinkedList<>();
        }

        String[] methodParams = methodStr.split(",");
        List<Object[]> params = new LinkedList<>();
        for (String methodParam : methodParams) {
            String str = methodParam.trim();
            // 字符串参数，包含单引号
            if (str.startsWith("'") && str.endsWith("'")) {
                params.add(new Object[]{str.substring(1, str.length() - 1), String.class});
            }
            // 布尔参数
            else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                params.add(new Object[]{Boolean.parseBoolean(str), Boolean.class});
            }
            // Long 参数
            else if (str.endsWith("L") || str.endsWith("l")) {
                params.add(new Object[]{Long.parseLong(str.substring(0, str.length() - 1)), Long.class});
            }
            // Double 参数
            else if (str.endsWith("D") || str.endsWith("d")) {
                params.add(new Object[]{Double.parseDouble(str.substring(0, str.length() - 1)), Double.class});
            }
            // Integer 参数
            else {
                params.add(new Object[]{Integer.parseInt(str), Integer.class});
            }
        }
        return params;
    }
}
