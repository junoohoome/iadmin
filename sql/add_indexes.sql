-- =============================================
-- iAdmin 数据库索引优化脚本
-- 执行此脚本添加性能优化索引
-- =============================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status, del_flag);
CREATE INDEX IF NOT EXISTS idx_user_dept_id ON sys_user(dept_id);

-- 角色表索引
CREATE INDEX IF NOT EXISTS idx_role_del_flag ON sys_role(del_flag);
CREATE INDEX IF NOT EXISTS idx_role_data_scope ON sys_role(data_scope);

-- 菜单表索引
CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_visible ON sys_menu(visible);

-- 操作日志表索引（已存在，确认）
-- CREATE INDEX idx_oper_time ON sys_oper_log(oper_time);
-- CREATE INDEX idx_status ON sys_oper_log(status);

-- 登录日志表索引（已存在，确认）
-- CREATE INDEX idx_login_time ON sys_logininfor(login_time);
-- CREATE INDEX idx_status ON sys_logininfor(status);
