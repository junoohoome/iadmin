package me.fjq.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传安全工具类
 * <p>
 * 提供文件类型验证、安全文件名生成等功能
 * 通过文件头（Magic Number）验证真实文件类型
 * </p>
 *
 * @author fjq
 */
@Slf4j
public class FileUploadUtils {

    /**
     * 允许的文件类型（通过 Magic Number 验证）
     * Key: 文件头十六进制前缀
     * Value: 文件扩展名
     */
    private static final Map<String, String> ALLOWED_FILE_TYPES = Map.of(
            "FFD8FF", "jpg",           // JPEG
            "89504E47", "png",         // PNG
            "47494638", "gif",         // GIF
            "424D", "bmp",             // BMP
            "25504446", "pdf",         // PDF
            "504B0304", "zip",         // ZIP/DOCX/XLSX
            "D0CF11E0", "doc"          // DOC/XLS (OLE2)
    );

    /**
     * 允许的文件扩展名（白名单）
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",  // 图片
            "pdf",                                        // 文档
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", // Office
            "txt", "csv"                                  // 文本
    );

    /**
     * 允许的 MIME 类型（白名单）
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv"
    );

    /**
     * 危险文件扩展名（黑名单）
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js",
            "jar", "class", "php", "asp", "aspx", "jsp", "jspx",
            "sh", "bash", "py", "pl", "rb", "cgi", "dll", "so"
    );

    private FileUploadUtils() {
        // 工具类不允许实例化
    }

    /**
     * 验证文件是否安全
     * <p>
     * 检查项：
     * 1. 文件扩展名白名单
     * 2. 文件 MIME 类型白名单
     * 3. 文件头（Magic Number）验证
     * 4. 危险扩展名黑名单
     * </p>
     *
     * @param file 上传的文件
     * @return 验证结果
     */
    public static FileValidationResult validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return FileValidationResult.fail("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return FileValidationResult.fail("文件名为空");
        }

        // 1. 获取文件扩展名
        String extension = getExtension(originalFilename).toLowerCase();

        // 2. 检查危险扩展名
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("检测到危险文件类型: {}", extension);
            return FileValidationResult.fail("不支持的文件类型: " + extension);
        }

        // 3. 检查扩展名白名单
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return FileValidationResult.fail("不支持的文件类型: " + extension);
        }

        // 4. 检查 MIME 类型
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("检测到不允许的 MIME 类型: {}", contentType);
            return FileValidationResult.fail("不支持的文件类型");
        }

        // 5. 验证文件头（Magic Number）
        try {
            String detectedType = detectFileType(file);
            if (detectedType == null) {
                log.warn("无法识别文件类型: {}", originalFilename);
                return FileValidationResult.fail("无法识别文件类型");
            }

            // 对于图片类型，验证必须匹配
            if (isImageExtension(extension) && !detectedType.equals(extension)) {
                log.warn("文件扩展名与实际类型不匹配: 扩展名={}, 实际={}", extension, detectedType);
                return FileValidationResult.fail("文件类型与扩展名不匹配");
            }

        } catch (IOException e) {
            log.error("文件类型检测失败", e);
            return FileValidationResult.fail("文件验证失败");
        }

        return FileValidationResult.success();
    }

    /**
     * 通过文件头检测文件真实类型
     *
     * @param file 上传的文件
     * @return 文件类型（扩展名），无法识别返回 null
     */
    public static String detectFileType(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = new byte[8];
            int read = is.read(bytes);

            if (read <= 0) {
                return null;
            }

            String hex = bytesToHex(bytes, read).toUpperCase();

            // 遍历已知的文件头进行匹配
            for (Map.Entry<String, String> entry : ALLOWED_FILE_TYPES.entrySet()) {
                if (hex.startsWith(entry.getKey().toUpperCase())) {
                    return entry.getValue();
                }
            }

            return null;
        }
    }

    /**
     * 生成安全的文件名
     * <p>
     * 使用 UUID 生成唯一文件名，保留原始扩展名
     * </p>
     *
     * @param originalFilename 原始文件名
     * @return 安全的文件名
     */
    public static String generateSafeFileName(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return UUID.randomUUID().toString().replace("-", "");
        }

        String extension = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        if (StringUtils.isNotBlank(extension)) {
            return uuid + "." + extension.toLowerCase();
        }

        return uuid;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点号）
     */
    public static String getExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }

        // 处理多个点号的情况，只取最后一个
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }

        return "";
    }

    /**
     * 检查是否为图片扩展名
     */
    private static boolean isImageExtension(String extension) {
        return Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

    /**
     * 获取允许的扩展名列表
     */
    public static Set<String> getAllowedExtensions() {
        return Collections.unmodifiableSet(ALLOWED_EXTENSIONS);
    }

    /**
     * 文件验证结果
     */
    public static class FileValidationResult {
        private final boolean valid;
        private final String message;

        private FileValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static FileValidationResult success() {
            return new FileValidationResult(true, "验证通过");
        }

        public static FileValidationResult fail(String message) {
            return new FileValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
