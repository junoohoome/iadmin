-- =============================================
-- iAdmin 后台管理系统数据库初始化脚本
-- 版本: 2.0.0
-- 日期: 2025-02-06
-- 说明: 基于 Spring Boot 3.2 + Java 21
-- =============================================

-- 创建数据库
-- CREATE DATABASE IF NOT EXISTS `iadmin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE `iadmin`;

-- =============================================
-- 1. 部门表
-- =============================================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id` bigint(20) DEFAULT 0 COMMENT '父部门ID',
    `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
    `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
    `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
    `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
    `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
    `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- =============================================
-- 2. 菜单权限表
-- =============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
    `parent_id` bigint(20) DEFAULT 0 COMMENT '父菜单ID',
    `sort` int(4) DEFAULT 0 COMMENT '显示顺序',
    `path` varchar(200) DEFAULT '' COMMENT '路由地址',
    `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
    `is_frame` int(1) DEFAULT 1 COMMENT '是否为外链（0是 1否）',
    `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
    `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- =============================================
-- 3. 角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(30) NOT NULL COMMENT '角色名称',
    `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
    `sort` int(4) NOT NULL COMMENT '显示顺序',
    `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定义数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）',
    `status` char(1) DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 4. 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_name` varchar(30) NOT NULL COMMENT '用户账号',
    `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
    `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
    `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
    `mobile` varchar(11) DEFAULT '' COMMENT '手机号码',
    `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
    `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
    `password` varchar(100) DEFAULT '' COMMENT '密码',
    `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
    `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
    `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
    `role_ids` varchar(255) DEFAULT NULL COMMENT '用户角色IDS',
    `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
    `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 5. 用户角色关联表
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 6. 角色菜单关联表
-- =============================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- =============================================
-- 7. 角色部门关联表（数据权限）
-- =============================================
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`, `dept_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与部门关联表（用于数据权限）';

-- =============================================
-- 初始化数据
-- =============================================

-- 8.1 部门数据
INSERT INTO `sys_dept` VALUES(100, 0, '0', 'iAdmin科技', 0, 'Admin', '15888888888', 'admin@iadmin.com', '0', '0', 'admin', NOW(), NULL, NULL, '总公司');

-- 8.2 角色数据
-- 密码: admin123 的 BCrypt 哈希值
INSERT INTO `sys_role` VALUES(1, '超级管理员', 'admin', 1, '1', '0', '0', 'admin', NOW(), NULL, NULL, '超级管理员');
INSERT INTO `sys_role` VALUES(2, '普通角色', 'common', 2, '2', '0', '0', 'admin', NOW(), NULL, NULL, '普通角色');

-- 8.3 用户数据
-- 密码: admin123
INSERT INTO `sys_user` VALUES(1, 'admin', '管理员', '00', 'admin@iadmin.com', '15888888888', '1', '', '$2a$10$tJq3g7E4acVh.a6nVVYPA.PcFQRT8O2ObMFL.wHqm1iJSNb.Y5DE6', '0', '0', '127.0.0.1', NOW(), '1', 100, '0', 'admin', NOW(), NULL, NULL, '管理员');
INSERT INTO `sys_user` VALUES(2, 'test', '测试用户', '00', 'test@iadmin.com', '15999999999', '0', '', '$2a$10$tJq3g7E4acVh.a6nVVYPA.PcFQRT8O2ObMFL.wHqm1iJSNb.Y5DE6', '0', '0', '', NULL, '2', 100, '0', 'admin', NOW(), NULL, NULL, '测试用户');

-- 8.4 用户角色关联
INSERT INTO `sys_user_role` VALUES(1, 1);
INSERT INTO `sys_user_role` VALUES(2, 2);

-- 8.5 菜单数据
INSERT INTO `sys_menu` VALUES(1, '系统管理', 0, 1, 'system', NULL, 1, 'M', '0', NULL, 'setting', 'admin', NOW(), NULL, NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES(2, '用户管理', 1, 1, 'user', 'system/user/index', 1, 'C', '0', 'system:user:list', 'user', 'admin', NOW(), NULL, NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES(3, '角色管理', 1, 2, 'role', 'system/role/index', 1, 'C', '0', 'system:role:list', 'peoples', 'admin', NOW(), NULL, NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES(4, '部门管理', 1, 3, 'dept', 'system/dept/index', 1, 'C', '0', 'system:dept:list', 'tree', 'admin', NOW(), NULL, NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES(5, '菜单管理', 1, 4, 'menu', 'system/menu/index', 1, 'C', '0', 'system:menu:list', 'list', 'admin', NOW(), NULL, NULL, '菜单管理菜单');

-- 8.6 角色菜单关联
INSERT INTO `sys_role_menu` VALUES(1, 1);
INSERT INTO `sys_role_menu` VALUES(1, 2);
INSERT INTO `sys_role_menu` VALUES(1, 3);
INSERT INTO `sys_role_menu` VALUES(1, 4);
INSERT INTO `sys_role_menu` VALUES(1, 5);

-- 8.7 角色部门关联（数据权限）
-- 普通角色只能访问总公司
INSERT INTO `sys_role_dept` VALUES(2, 100);

-- =============================================
-- 索引优化
-- =============================================
#CREATE INDEX `idx_user_name` ON `sys_user`(`user_name`);
CREATE INDEX `idx_dept_id` ON `sys_user`(`dept_id`);
CREATE INDEX `idx_login_date` ON `sys_user`(`login_date`);
CREATE INDEX `idx_data_scope` ON `sys_role`(`data_scope`);

-- =============================================
-- 初始化完成
-- =============================================
-- 默认账号: admin / admin123
-- 测试账号: test / admin123
