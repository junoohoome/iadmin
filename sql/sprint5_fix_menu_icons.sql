-- =============================================
-- Sprint 5: 修复菜单图标和添加缓存监控菜单
-- 版本: 2.0.0
-- 日期: 2025-02-17
-- 说明:
--   1. 修复系统管理图标（setting -> system）
--   2. 添加缓存监控菜单
-- =============================================

-- 1. 修复系统管理图标
UPDATE `sys_menu` SET icon = 'system' WHERE menu_id = 1 AND menu_name = '系统管理';

-- 2. 添加缓存监控菜单（系统监控的子菜单）
INSERT INTO `sys_menu` (menu_id, menu_name, parent_id, sort, path, component, is_frame, menu_type, visible, perms, icon, create_by, create_time, remark)
VALUES(103, '缓存监控', 100, 3, 'cache', 'monitor/cache/index', 1, 'C', '0', 'monitor:cache:list', 'redis', 'admin', NOW(), '缓存监控菜单');

-- 3. 为角色添加缓存监控权限
INSERT INTO `sys_role_menu` (role_id, menu_id) VALUES(1, 103);
INSERT INTO `sys_role_menu` (role_id, menu_id) VALUES(2, 103);

-- =============================================
-- 执行说明
-- =============================================
-- 1. 连接到 MySQL 数据库: mysql -u root -p iadmin
-- 2. 执行此脚本: source sprint5_fix_menu_icons.sql
-- =============================================
