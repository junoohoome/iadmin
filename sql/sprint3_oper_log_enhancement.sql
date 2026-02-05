-- =============================================
-- Sprint 3: 操作日志增强 SQL 迁移脚本
-- 版本: 2.0.0
-- 日期: 2025-02-05
-- =============================================

-- 扩展 sys_oper_log 表，添加新字段
-- =============================================

-- 检查并添加 request_param 字段
ALTER TABLE `sys_oper_log`
ADD COLUMN IF NOT EXISTS `request_param` longtext COMMENT '请求参数';

-- 检查并添加 response_result 字段
ALTER TABLE `sys_oper_log`
ADD COLUMN IF NOT EXISTS `response_result` longtext COMMENT '响应结果';

-- 检查并添加 cost_time 字段
ALTER TABLE `sys_oper_log`
ADD COLUMN IF NOT EXISTS `cost_time` bigint(20) DEFAULT 0 COMMENT '执行时长(毫秒)';

-- 检查并添加 status 字段（请求状态）
ALTER TABLE `sys_oper_log`
ADD COLUMN IF NOT EXISTS `status` int(1) DEFAULT 0 COMMENT '操作状态（0正常 1异常）';

-- 检查并添加 error_msg 字段（错误信息）
ALTER TABLE `sys_oper_log`
ADD COLUMN IF NOT EXISTS `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息';

-- 为新增字段添加索引以提升查询性能
ALTER TABLE `sys_oper_log`
ADD INDEX IF NOT EXISTS `idx_status` (`status`);

ALTER TABLE `sys_oper_log`
ADD INDEX IF NOT EXISTS `idx_cost_time` (`cost_time`);

-- =============================================
-- 字段说明
-- =============================================
-- request_param: 存储请求参数的 JSON 字符串（敏感信息已脱敏）
-- response_result: 存储响应结果的 JSON 字符串
-- cost_time: 存储接口执行时长，单位毫秒
-- status: 0=正常，1=异常
-- error_msg: 当 status=1 时存储异常堆栈信息
