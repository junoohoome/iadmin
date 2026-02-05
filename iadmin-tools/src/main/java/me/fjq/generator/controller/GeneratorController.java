package me.fjq.generator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.generator.domain.GenConfig;
import me.fjq.generator.domain.GenTable;
import me.fjq.generator.service.GeneratorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 *
 * @author fjq
 * @since 2025-02-05
 */
@RestController
@RequestMapping("/generator")
@AllArgsConstructor
@Tag(name = "代码生成", description = "代码生成管理接口")
public class GeneratorController {

    private final GeneratorService generatorService;

    /**
     * 查询所有数据库表
     */
    @GetMapping("/tables")
    @Operation(summary = "查询所有表")
    public HttpResult<List<GenTable>> listTables() {
        List<GenTable> tables = generatorService.listTables();
        return HttpResult.ok(tables);
    }

    /**
     * 根据表名获取表详细信息
     */
    @GetMapping("/table/{tableName}")
    @Operation(summary = "获取表详情")
    public HttpResult<GenTable> getTableInfo(
            @Parameter(description = "表名") @PathVariable String tableName) {
        GenTable table = generatorService.getTableInfo(tableName);
        return HttpResult.ok(table);
    }

    /**
     * 预览生成的代码
     */
    @PostMapping("/preview")
    @Operation(summary = "预览代码")
    public HttpResult<Map<String, String>> previewCode(@RequestBody GenConfig genConfig) {
        Map<String, String> codeMap = generatorService.previewCode(genConfig);
        return HttpResult.ok(codeMap);
    }

    /**
     * 生成代码（返回文件内容）
     */
    @PostMapping("/generate")
    @Operation(summary = "生成代码")
    public HttpResult<Map<String, String>> generateCode(@RequestBody GenConfig genConfig) {
        Map<String, String> codeMap = generatorService.generateCode(genConfig);
        return HttpResult.ok(codeMap);
    }

    /**
     * 下载代码（ZIP包）
     */
    @PostMapping("/download")
    @Operation(summary = "下载代码")
    public byte[] downloadCode(@RequestBody GenConfig genConfig) {
        return generatorService.downloadCode(genConfig);
    }

    /**
     * 批量生成代码
     */
    @PostMapping("/batch")
    @Operation(summary = "批量生成代码")
    public HttpResult<Map<String, String>> batchGenerateCode(@RequestBody List<GenConfig> genConfigs) {
        Map<String, String> allCode = new java.util.LinkedHashMap<>();
        for (GenConfig config : genConfigs) {
            Map<String, String> codeMap = generatorService.generateCode(config);
            allCode.putAll(codeMap);
        }
        return HttpResult.ok(allCode);
    }
}
