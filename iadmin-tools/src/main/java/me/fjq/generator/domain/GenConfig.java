package me.fjq.generator.domain;

/**
 * 代码生成配置
 *
 * @author fjq
 * @since 2025-02-05
 */
public record GenConfig(
        /**
         * 作者名称
         */
        String authorName,

        /**
         * 生成包路径
         */
        String packageName,

        /**
         * 模块名
         */
        String moduleName,

        /**
         * 业务名
         */
        String businessName,

        /**
         * 功能名
         */
        String functionName,

        /**
         * 表前缀
         */
        String tablePrefix,

        /**
         * 生成路径
         */
        String genPath
) {
    /**
     * 默认配置
     */
    public static GenConfig defaultConfig() {
        return new GenConfig("fjq", "me.fjq", "system", "", "", "", "");
    }
}
