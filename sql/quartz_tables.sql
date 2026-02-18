-- =============================================
-- Quartz 定时任务模块数据库表
-- =============================================

-- ----------------------------
-- 1. 定时任务配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
    job_id              BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '任务ID',
    job_name            VARCHAR(64)     DEFAULT ''                 COMMENT '任务名称',
    job_group           VARCHAR(64)     DEFAULT 'DEFAULT'          COMMENT '任务组名',
    invoke_target       VARCHAR(500)    NOT NULL                   COMMENT '调用目标字符串',
    cron_expression     VARCHAR(255)    DEFAULT ''                 COMMENT 'cron执行表达式',
    misfire_policy      VARCHAR(20)     DEFAULT '3'                COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent          CHAR(1)         DEFAULT '1'                COMMENT '是否并发执行（0允许 1禁止）',
    status              CHAR(1)         DEFAULT '0'                COMMENT '状态（0正常 1暂停）',
    create_by           VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
    create_time         DATETIME                                   COMMENT '创建时间',
    update_by           VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
    update_time         DATETIME                                   COMMENT '更新时间',
    remark              VARCHAR(500)    DEFAULT ''                 COMMENT '备注信息',
    PRIMARY KEY (job_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT = '定时任务调度表';

-- ----------------------------
-- 2. 定时任务执行日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log (
    job_log_id          BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '任务日志ID',
    job_name            VARCHAR(64)     NOT NULL                   COMMENT '任务名称',
    job_group           VARCHAR(64)     NOT NULL                   COMMENT '任务组名',
    invoke_target       VARCHAR(500)    NOT NULL                   COMMENT '调用目标字符串',
    job_message         VARCHAR(500)                               COMMENT '日志信息',
    status              CHAR(1)         DEFAULT '0'                COMMENT '执行状态（0正常 1失败）',
    exception_info      VARCHAR(2000)   DEFAULT ''                 COMMENT '异常信息',
    create_time         DATETIME                                   COMMENT '创建时间',
    PRIMARY KEY (job_log_id)
) ENGINE=InnoDB COMMENT = '定时任务调度日志表';

-- ----------------------------
-- 3. 初始化任务数据
-- ----------------------------
INSERT INTO sys_job VALUES(1, '系统默认（无参）', 'DEFAULT', 'demoTask.noParams', '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_job VALUES(2, '系统默认（有参）', 'DEFAULT', 'demoTask.params(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_job VALUES(3, '系统默认（多参）', 'DEFAULT', 'demoTask.multipleParams(\'ry\', true, 2000L, 316.50D)', '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', NULL, '');


-- =============================================
-- Quartz 官方表结构 (InnoDB)
-- =============================================

-- ----------------------------
-- 1. 存储已配置的 Job 的详细信息
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
CREATE TABLE QRTZ_JOB_DETAILS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    job_name             VARCHAR(200)    NOT NULL            COMMENT '集群中job的名字',
    job_group            VARCHAR(200)    NOT NULL            COMMENT '集群中job的所属组的名字',
    description          VARCHAR(250)    NULL                COMMENT '描述',
    job_class_name       VARCHAR(250)    NOT NULL            COMMENT '作业程序类名',
    is_durable           VARCHAR(1)      NOT NULL            COMMENT '是否持久化',
    is_nonconcurrent     VARCHAR(1)      NOT NULL            COMMENT '是否并发',
    is_update_data       VARCHAR(1)      NOT NULL            COMMENT '是否更新数据',
    requests_recovery    VARCHAR(1)      NOT NULL            COMMENT '是否接受恢复执行',
    job_data             BLOB            NULL                COMMENT '存放持久化job对象',
    PRIMARY KEY (sched_name, job_name, job_group)
) ENGINE=InnoDB COMMENT = '任务详细信息表';

-- ----------------------------
-- 2. 存储已配置的 Trigger 的详细信息
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
CREATE TABLE QRTZ_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    job_name             VARCHAR(200)    NOT NULL            COMMENT '作业名称',
    job_group            VARCHAR(200)    NOT NULL            COMMENT '作业组名',
    description          VARCHAR(250)    NULL                COMMENT '描述',
    next_fire_time       BIGINT(13)      NULL                COMMENT '下次触发时间',
    prev_fire_time       BIGINT(13)      NULL                COMMENT '上次触发时间',
    priority             INTEGER         NULL                COMMENT '优先级',
    trigger_state        VARCHAR(16)     NOT NULL            COMMENT '触发器状态',
    trigger_type         VARCHAR(8)      NOT NULL            COMMENT '触发器类型',
    start_time           BIGINT(13)      NOT NULL            COMMENT '开始时间',
    end_time             BIGINT(13)      NULL                COMMENT '结束时间',
    calendar_name        VARCHAR(200)    NULL                COMMENT '日程表名称',
    misfire_instr        SMALLINT(2)     NULL                COMMENT '补偿执行策略',
    job_data             BLOB            NULL                COMMENT '存放持久化job对象',
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, job_name, job_group)
        REFERENCES QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) ENGINE=InnoDB COMMENT = '触发器信息表';

-- ----------------------------
-- 3. 简单触发器信息表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    repeat_count         BIGINT(7)       NOT NULL            COMMENT '重复次数',
    repeat_interval      BIGINT(12)      NOT NULL            COMMENT '重复间隔时间',
    times_triggered      BIGINT(10)      NOT NULL            COMMENT '已触发次数',
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB COMMENT = '简单触发器信息表';

-- ----------------------------
-- 4. Cron 触发器信息表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
CREATE TABLE QRTZ_CRON_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    cron_expression      VARCHAR(200)    NOT NULL            COMMENT 'cron表达式',
    time_zone_id         VARCHAR(80)                         COMMENT '时区',
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB COMMENT = 'Cron触发器信息表';

-- ----------------------------
-- 5. 触发器博客信息表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
CREATE TABLE QRTZ_BLOB_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    blob_data            BLOB            NULL                COMMENT '存放持久化trigger对象',
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB COMMENT = '触发器博客信息表';

-- ----------------------------
-- 6. 日历信息表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_CALENDARS;
CREATE TABLE QRTZ_CALENDARS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    calendar_name        VARCHAR(200)    NOT NULL            COMMENT '日历名称',
    calendar             BLOB            NOT NULL            COMMENT '存放持久化calendar对象',
    PRIMARY KEY (sched_name, calendar_name)
) ENGINE=InnoDB COMMENT = '日历信息表';

-- ----------------------------
-- 7. 暂停触发器组表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    PRIMARY KEY (sched_name, trigger_group)
) ENGINE=InnoDB COMMENT = '暂停触发器组表';

-- ----------------------------
-- 8. 已触发触发器表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
CREATE TABLE QRTZ_FIRED_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    entry_id             VARCHAR(95)     NOT NULL            COMMENT '条目ID',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    instance_name        VARCHAR(200)    NOT NULL            COMMENT '实例名称',
    fired_time           BIGINT(13)      NOT NULL            COMMENT '触发时间',
    sched_time           BIGINT(13)      NOT NULL            COMMENT '调度时间',
    priority             INTEGER         NOT NULL            COMMENT '优先级',
    state                VARCHAR(16)     NOT NULL            COMMENT '状态',
    job_name             VARCHAR(200)    NULL                COMMENT '作业名称',
    job_group            VARCHAR(200)    NULL                COMMENT '作业组名',
    is_nonconcurrent     VARCHAR(1)      NULL                COMMENT '是否并发',
    requests_recovery    VARCHAR(1)      NULL                COMMENT '是否接受恢复执行',
    PRIMARY KEY (sched_name, entry_id)
) ENGINE=InnoDB COMMENT = '已触发触发器表';

-- ----------------------------
-- 9. 调度器状态表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
CREATE TABLE QRTZ_SCHEDULER_STATE (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    instance_name        VARCHAR(200)    NOT NULL            COMMENT '实例名称',
    last_checkin_time    BIGINT(13)      NOT NULL            COMMENT '上次检查时间',
    checkin_interval     BIGINT(13)      NOT NULL            COMMENT '检查间隔时间',
    PRIMARY KEY (sched_name, instance_name)
) ENGINE=InnoDB COMMENT = '调度器状态表';

-- ----------------------------
-- 10. 锁表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_LOCKS;
CREATE TABLE QRTZ_LOCKS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    lock_name            VARCHAR(40)     NOT NULL            COMMENT '锁名称',
    PRIMARY KEY (sched_name, lock_name)
) ENGINE=InnoDB COMMENT = '锁表';

-- ----------------------------
-- 11. 简单属性触发器表
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
    sched_name           VARCHAR(120)    NOT NULL            COMMENT '调度名称',
    trigger_name         VARCHAR(200)    NOT NULL            COMMENT '触发器名称',
    trigger_group        VARCHAR(200)    NOT NULL            COMMENT '触发器组名',
    str_prop_1           VARCHAR(512)    NULL                COMMENT '字符串属性1',
    str_prop_2           VARCHAR(512)    NULL                COMMENT '字符串属性2',
    str_prop_3           VARCHAR(512)    NULL                COMMENT '字符串属性3',
    int_prop_1           INT             NULL                COMMENT '整型属性1',
    int_prop_2           INT             NULL                COMMENT '整型属性2',
    long_prop_1          BIGINT          NULL                COMMENT '长整型属性1',
    long_prop_2          BIGINT          NULL                COMMENT '长整型属性2',
    dec_prop_1           NUMERIC(13,4)   NULL                COMMENT '小数属性1',
    dec_prop_2           NUMERIC(13,4)   NULL                COMMENT '小数属性2',
    bool_prop_1          VARCHAR(1)      NULL                COMMENT '布尔属性1',
    bool_prop_2          VARCHAR(1)      NULL                COMMENT '布尔属性2',
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB COMMENT = '简单属性触发器表';
