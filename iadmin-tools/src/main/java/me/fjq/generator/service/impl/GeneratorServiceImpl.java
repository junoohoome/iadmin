package me.fjq.generator.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.generator.domain.GenConfig;
import me.fjq.generator.domain.GenTable;
import me.fjq.generator.domain.GenTableColumn;
import me.fjq.generator.service.GeneratorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现类
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
@Service
@AllArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 数据库类型到Java类型映射
     */
    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
            Map.entry("tinyint", "Integer"),
            Map.entry("smallint", "Integer"),
            Map.entry("mediumint", "Integer"),
            Map.entry("int", "Integer"),
            Map.entry("integer", "Integer"),
            Map.entry("bigint", "Long"),
            Map.entry("float", "Float"),
            Map.entry("double", "Double"),
            Map.entry("decimal", "java.math.BigDecimal"),
            Map.entry("char", "String"),
            Map.entry("varchar", "String"),
            Map.entry("text", "String"),
            Map.entry("longtext", "String"),
            Map.entry("date", "java.time.LocalDate"),
            Map.entry("datetime", "java.time.LocalDateTime"),
            Map.entry("timestamp", "java.time.LocalDateTime")
    );

    private static final String DATABASE = "iadmin";

    @Override
    public List<GenTable> listTables() {
        String sql = """
            SELECT table_name, table_comment
            FROM information_schema.tables
            WHERE table_schema = ?
              AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;

        List<GenTable> tables = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            String tableName = rs.getString("table_name");
            String tableComment = rs.getString("table_comment");
            tables.add(new GenTable(
                0L,
                tableName,
                tableComment,
                tableNameToClassName(tableName),
                "",
                tableNameToBusinessName(tableName, null),
                "",
                "",
                "",
                new ArrayList<>(),
                null,
                ""
            ));
        }, DATABASE);

        return tables;
    }

    @Override
    public GenTable getTableInfo(String tableName) {
        String tableSql = """
            SELECT table_name, table_comment
            FROM information_schema.tables
            WHERE table_schema = ?
              AND table_name = ?
            """;

        List<GenTable> resultList = new ArrayList<>();
        jdbcTemplate.query(tableSql, rs -> {
            String tName = rs.getString("table_name");
            String tComment = rs.getString("table_comment");
            resultList.add(new GenTable(
                0L,
                tName,
                tComment,
                "",
                "",
                "",
                "",
                "",
                "",
                new ArrayList<>(),
                null,
                ""
            ));
        }, DATABASE, tableName);

        if (resultList.isEmpty()) {
            return null;
        }

        GenTable baseTable = resultList.get(0);

        // 查询字段信息
        String columnSql = """
            SELECT column_name, column_comment, data_type,
                   is_nullable, column_key, extra,
                   ordinal_position
            FROM information_schema.columns
            WHERE table_schema = ?
              AND table_name = ?
            ORDER BY ordinal_position
            """;

        List<GenTableColumn> columns = new ArrayList<>();
        jdbcTemplate.query(columnSql, rs -> {
            String columnName = rs.getString("column_name");
            String columnComment = rs.getString("column_comment");
            String columnType = rs.getString("data_type");
            String isNullable = rs.getString("is_nullable");
            String columnKey = rs.getString("column_key");
            String extra = rs.getString("extra");

            GenTableColumn column = new GenTableColumn(
                0L,
                0L,
                columnName,
                columnComment,
                columnType,
                TYPE_MAP.getOrDefault(columnType.toLowerCase(), "String"),
                toCamelCase(columnName),
                "PRI".equals(columnKey),
                extra != null && extra.contains("auto_increment"),
                "NO".equals(isNullable),
                true,
                true,
                true,
                false,
                "",
                "",
                ""
            );
            columns.add(column);
        }, DATABASE, tableName);

        // 设置主键列
        GenTableColumn pkColumn = columns.stream()
                .filter(GenTableColumn::isPk)
                .findFirst()
                .orElse(null);

        return new GenTable(
            baseTable.tableId(),
            baseTable.tableName(),
            baseTable.tableComment(),
            tableNameToClassName(tableName),
            "",
            tableNameToBusinessName(tableName, null),
            "",
            "",
            "",
            columns,
            pkColumn,
            ""
        );
    }

    @Override
    public Map<String, String> previewCode(GenConfig genConfig) {
        GenTable table = getTableInfo(genConfig.businessName());
        if (table == null) {
            return Collections.emptyMap();
        }

        return generateCodeFiles(table, genConfig);
    }

    @Override
    public Map<String, String> generateCode(GenConfig genConfig) {
        return previewCode(genConfig);
    }

    @Override
    public byte[] downloadCode(GenConfig genConfig) {
        Map<String, String> codeFiles = generateCode(genConfig);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
                String fileName = entry.getKey();
                String content = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("生成代码ZIP包失败", e);
            throw new RuntimeException("生成代码ZIP包失败", e);
        }
    }

    @Override
    public String tableNameToClassName(String tableName) {
        return toCamelCase(tableName);
    }

    @Override
    public String tableNameToBusinessName(String tableName, String prefix) {
        String name = tableName;
        if (StrUtil.isNotBlank(prefix)) {
            name = tableName.replaceFirst("^" + prefix, "");
        }
        return toCamelCase(name);
    }

    private Map<String, String> generateCodeFiles(GenTable table, GenConfig config) {
        Map<String, String> files = new LinkedHashMap<>();

        String packageName = "me.fjq." + config.moduleName();
        String businessName = StrUtil.isNotBlank(config.businessName())
            ? config.businessName()
            : table.businessName();
        String className = table.className();

        // 生成Entity
        files.put("java/" + packageName.replace('.', '/') + "/entity/" + className + ".java",
                generateEntity(table, packageName, config));

        // 生成Mapper
        files.put("java/" + packageName.replace('.', '/') + "/mapper/" + className + "Mapper.java",
                generateMapper(table, packageName, config));

        // 生成Service
        files.put("java/" + packageName.replace('.', '/') + "/service/" + className + "Service.java",
                generateService(table, packageName, config));

        // 生成ServiceImpl
        files.put("java/" + packageName.replace('.', '/') + "/service/impl/" + className + "ServiceImpl.java",
                generateServiceImpl(table, packageName, config));

        // 生成Controller
        files.put("java/" + packageName.replace('.', '/') + "/controller/" + className + "Controller.java",
                generateController(table, packageName, config));

        return files;
    }

    private String generateEntity(GenTable table, String packageName, GenConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(".entity;\n\n");
        sb.append("import lombok.Data;\n\n");
        sb.append("import java.io.Serializable");
        if (hasLocalDate(table)) {
            sb.append(";\nimport java.time.LocalDate");
        }
        if (hasLocalDateTime(table)) {
            if (hasLocalDate(table)) {
                sb.append(";\n");
            } else {
                sb.append(";\n");
            }
            sb.append("import java.time.LocalDateTime");
        }
        sb.append(";\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append("\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(config.authorName()).append("\n");
        sb.append(" * @since ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append(" */\n");
        sb.append("@Data\n");
        sb.append("public class ").append(table.className()).append(" implements Serializable {\n\n");
        sb.append("    private static final long serialVersionUID = 1L;\n\n");

        for (GenTableColumn column : table.columns()) {
            sb.append("    /**\n");
            sb.append("     * ").append(column.columnComment() != null ? column.columnComment() : column.columnName()).append("\n");
            sb.append("     */\n");
            if (column.isPk()) {
                sb.append("    /**\n");
                sb.append("     * 主键\n");
                sb.append("     */\n");
            }
            sb.append("    private ").append(column.javaType()).append(" ").append(column.javaField()).append(";\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateMapper(GenTable table, String packageName, GenConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(".mapper;\n\n");
        sb.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n");
        sb.append("import ").append(packageName).append(".entity.").append(table.className()).append(";\n");
        sb.append("import org.apache.ibatis.annotations.Mapper;\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append(" Mapper\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(config.authorName()).append("\n");
        sb.append(" * @since ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append(" */\n");
        sb.append("@Mapper\n");
        sb.append("public interface ").append(table.className()).append("Mapper extends BaseMapper<").append(table.className()).append("> {\n\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateService(GenTable table, String packageName, GenConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(".service;\n\n");
        sb.append("import com.baomidou.mybatisplus.extension.service.IService;\n");
        sb.append("import ").append(packageName).append(".entity.").append(table.className()).append(";\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append(" 服务接口\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(config.authorName()).append("\n");
        sb.append(" * @since ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append(" */\n");
        sb.append("public interface ").append(table.className()).append("Service extends IService<").append(table.className()).append("> {\n\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateServiceImpl(GenTable table, String packageName, GenConfig config) {
        String lowerClassName = Character.toLowerCase(table.className().charAt(0)) + table.className().substring(1);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(".service.impl;\n\n");
        sb.append("import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n");
        sb.append("import lombok.AllArgsConstructor;\n");
        sb.append("import ").append(packageName).append(".entity.").append(table.className()).append(";\n");
        sb.append("import ").append(packageName).append(".mapper.").append(table.className()).append("Mapper;\n");
        sb.append("import ").append(packageName).append(".service.").append(table.className()).append("Service;\n");
        sb.append("import org.springframework.stereotype.Service;\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append(" 服务实现类\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(config.authorName()).append("\n");
        sb.append(" * @since ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append(" */\n");
        sb.append("@Service\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("public class ").append(table.className()).append("ServiceImpl extends ServiceImpl<").append(table.className()).append("Mapper, ")
                .append(table.className()).append("> implements ").append(table.className()).append("Service {\n\n");
        sb.append("    private final ").append(table.className()).append("Mapper ").append(lowerClassName).append("Mapper;\n\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateController(GenTable table, String packageName, GenConfig config) {
        String lowerBusinessName = Character.toLowerCase(table.businessName().charAt(0)) + table.businessName().substring(1);
        String functionName = StrUtil.isNotBlank(config.functionName()) ? config.functionName() : (table.tableComment() != null ? table.tableComment() : table.tableName());

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(".controller;\n\n");
        sb.append("import io.swagger.v3.oas.annotations.Operation;\n");
        sb.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        sb.append("import lombok.AllArgsConstructor;\n");
        sb.append("import me.fjq.core.HttpResult;\n");
        sb.append("import ").append(packageName).append(".entity.").append(table.className()).append(";\n");
        sb.append("import ").append(packageName).append(".service.").append(table.className()).append("Service;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n\n");
        sb.append("/**\n");
        sb.append(" * ").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append(" 控制器\n");
        sb.append(" *\n");
        sb.append(" * @author ").append(config.authorName()).append("\n");
        sb.append(" * @since ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append(" */\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"/").append(config.moduleName()).append("/").append(lowerBusinessName).append("\")\n");
        sb.append("@AllArgsConstructor\n");
        sb.append("@Tag(name = \"").append(functionName).append("\", description = \"").append(table.tableComment() != null ? table.tableComment() : table.tableName()).append("管理\")\n");
        sb.append("public class ").append(table.className()).append("Controller {\n\n");
        sb.append("    private final ").append(table.className()).append("Service ").append(lowerBusinessName).append("Service;\n\n");
        sb.append("    @GetMapping(\"/list\")\n");
        sb.append("    @Operation(summary = \"查询列表\")\n");
        sb.append("    public HttpResult<?> list() {\n");
        sb.append("        return HttpResult.ok(").append(lowerBusinessName).append("Service.list());\n");
        sb.append("    }\n\n");
        sb.append("    @GetMapping(\"/{id}\")\n");
        sb.append("    @Operation(summary = \"根据ID查询\")\n");
        sb.append("    public HttpResult<?> getById(@PathVariable Long id) {\n");
        sb.append("        return HttpResult.ok(").append(lowerBusinessName).append("Service.getById(id));\n");
        sb.append("    }\n\n");
        sb.append("    @PostMapping\n");
        sb.append("    @Operation(summary = \"新增\")\n");
        sb.append("    public HttpResult<?> save(@RequestBody ").append(table.className()).append(" ").append(lowerBusinessName).append(") {\n");
        sb.append("        return HttpResult.ok(").append(lowerBusinessName).append("Service.save(").append(lowerBusinessName).append("));\n");
        sb.append("    }\n\n");
        sb.append("    @PutMapping\n");
        sb.append("    @Operation(summary = \"修改\")\n");
        sb.append("    public HttpResult<?> update(@RequestBody ").append(table.className()).append(" ").append(lowerBusinessName).append(") {\n");
        sb.append("        return HttpResult.ok(").append(lowerBusinessName).append("Service.updateById(").append(lowerBusinessName).append("));\n");
        sb.append("    }\n\n");
        sb.append("    @DeleteMapping(\"/{id}\")\n");
        sb.append("    @Operation(summary = \"删除\")\n");
        sb.append("    public HttpResult<?> delete(@PathVariable Long id) {\n");
        sb.append("        return HttpResult.ok(").append(lowerBusinessName).append("Service.removeById(id));\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String toCamelCase(String str) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        String[] parts = str.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (StrUtil.isNotBlank(part)) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }

    private boolean hasLocalDate(GenTable table) {
        return table.columns().stream()
                .anyMatch(c -> "java.time.LocalDate".equals(c.javaType()));
    }

    private boolean hasLocalDateTime(GenTable table) {
        return table.columns().stream()
                .anyMatch(c -> "java.time.LocalDateTime".equals(c.javaType()));
    }
}
