-- =============================================
-- Sprint 4: 补充缺失的菜单数据
-- 版本: 2.0.0
-- 日期: 2025-02-16
-- 说明: 添加字典管理和系统监控相关菜单
-- =============================================

-- 添加字典管理菜单（系统管理的子菜单）
INSERT INTO `sys_menu` VALUES(6, '字典管理', 1, 5, 'dict', 'system/dict/index', 1, 'C', '0', 'system:dict:list', 'education', 'admin', NOW(), NULL, NULL, '字典管理菜单');

-- 添加系统监控目录
INSERT INTO `sys_menu` VALUES(100, '系统监控', 0, 2, 'monitor', NULL, 1, 'M', '0', NULL, 'monitor', 'admin', NOW(), NULL, NULL, '系统监控目录');

-- 添加操作日志菜单（系统监控的子菜单）
INSERT INTO `sys_menu` VALUES(101, '操作日志', 100, 1, 'oper-log', 'monitor/oper-log/index', 1, 'C', '0', 'monitor:operlog:list', 'log', 'admin', NOW(), NULL, NULL, '操作日志菜单');

-- 添加登录日志菜单（系统监控的子菜单）
INSERT INTO `sys_menu` VALUES(102, '登录日志', 100, 2, 'login-log', 'monitor/login-log/index', 1, 'C', '0', 'monitor:loginlog:list', 'logininfor', 'admin', NOW(), NULL, NULL, '登录日志菜单');

-- 为超级管理员角色添加新菜单的权限关联
INSERT INTO `sys_role_menu` VALUES(1, 6);   -- 字典管理
INSERT INTO `sys_role_menu` VALUES(1, 100); -- 系统监控目录
INSERT INTO `sys_role_menu` VALUES(1, 101); -- 操作日志
INSERT INTO `sys_role_menu` VALUES(1, 102); -- 登录日志

-- 为普通角色添加基本菜单权限（可选）
INSERT INTO `sys_role_menu` VALUES(2, 6);   -- 字典管理
INSERT INTO `sys_role_menu` VALUES(2, 100); -- 系统监控目录
INSERT INTO `sys_role_menu` VALUES(2, 101); -- 操作日志
INSERT INTO `sys_role_menu` VALUES(2, 102); -- 登录日志

-- =============================================
-- 执行说明
-- =============================================
-- 1. 连接到 MySQL 数据库: mysql -u root -p iadmin
-- 2. 执行此脚本: source sprint4_add_missing_menus.sql
-- 或者直接复制粘贴执行
-- =============================================
