package me.fjq.generator.service;

import me.fjq.generator.domain.GenConfig;
import me.fjq.generator.domain.GenTable;

import java.util.List;
import java.util.Map;

/**
 * 代码生成服务接口
 *
 * @author fjq
 * @since 2025-02-05
 */
public interface GeneratorService {

    /**
     * 查询所有数据库表
     *
     * @return 表列表
     */
    List<GenTable> listTables();

    /**
     * 根据表名获取表详细信息（包含字段信息）
     *
     * @param tableName 表名
     * @return 表信息
     */
    GenTable getTableInfo(String tableName);

    /**
     * 预览生成的代码
     *
     * @param genConfig 生成配置
     * @return 文件名到代码内容的映射
     */
    Map<String, String> previewCode(GenConfig genConfig);

    /**
     * 生成代码并返回文件内容
     *
     * @param genConfig 生成配置
     * @return 文件名到代码内容的映射
     */
    Map<String, String> generateCode(GenConfig genConfig);

    /**
     * 生成代码ZIP包字节数组
     *
     * @param genConfig 生成配置
     * @return ZIP包字节数组
     */
    byte[] downloadCode(GenConfig genConfig);

    /**
     * 根据表名转换为类名
     *
     * @param tableName 表名
     * @return 类名
     */
    String tableNameToClassName(String tableName);

    /**
     * 根据表名转换为业务名
     *
     * @param tableName 表名
     * @param prefix    前缀
     * @return 业务名
     */
    String tableNameToBusinessName(String tableName, String prefix);
}
