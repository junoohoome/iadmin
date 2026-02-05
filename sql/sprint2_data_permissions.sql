-- =============================================
-- Sprint 2: 数据权限功能 SQL 迁移脚本
-- 版本: 2.0.0
-- 日期: 2025-02-05
-- =============================================

-- 1. 创建角色与部门关联表
-- =============================================
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`, `dept_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与部门关联表（用于数据权限）';

-- 2. 修改 sys_role 表的 data_scope 字段注释
-- =============================================
-- 注意：如果字段已存在，只修改注释
ALTER TABLE `sys_role`
MODIFY COLUMN `data_scope` char(1) DEFAULT '1'
COMMENT '数据范围（1：全部数据权限 2：自定义数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）';

-- 3. 确保 sys_user 表有最后登录信息字段
-- =============================================
-- 注意：如果字段已存在，跳过
ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `login_date` datetime DEFAULT NULL COMMENT '最后登录时间';

ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `login_ip` varchar(128) DEFAULT NULL COMMENT '最后登录IP';

-- 4. 插入初始化数据
-- =============================================

-- 示例：为管理员角色配置全部数据权限
-- 假设管理员角色ID为1，需要根据实际情况调整
-- INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (1, 100);

-- 5. 创建索引优化查询性能
-- =============================================
-- 为 sys_user 表的登录相关字段添加索引
ALTER TABLE `sys_user`
ADD INDEX IF NOT EXISTS `idx_login_date` (`login_date`);

-- 为 sys_role 表的数据权限字段添加索引
ALTER TABLE `sys_role`
ADD INDEX IF NOT EXISTS `idx_data_scope` (`data_scope`);

-- =============================================
-- 数据权限说明
-- =============================================
-- 1. 全部数据权限：可以查看所有数据，不添加任何过滤条件
-- 2. 自定义数据权限：只能查看 sys_role_dept 中配置的部门数据
-- 3. 本部门数据权限：只能查看本部门的数据
-- 4. 本部门及以下数据权限：可以查看本部门及其子部门的数据
-- 5. 仅本人数据权限：只能查看自己的数据
