-- 建表脚本

-- 创建库
create database if not exists shierbi;

-- 切换库
use shierbi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment 'user-普通用户 admin-管理员',
    gender       varchar(256) default '男'              null comment '性别 男 女',
    phone        varchar(128)                           null comment '电话',
    email        varchar(512)                           null comment '邮箱',
    userStatus   int          default 0                 not null comment '状态 0 - 正常 1-注销 2-封号',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;


-- 用户编号表
create table if not exists user_code
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userAccount (id)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表信息表
create table if not exists chart
(
    id          bigint auto_increment comment 'id' primary key,
    goal        text                                   null comment '分析目标',
    chartName   varchar(256)                           null comment '图表名称',
    chartData   text                                   null comment '图表数据',
    chartType   varchar(256)                           null comment '图表类型',
    genChart    text                                   null comment '生成的图表信息',
    genResult   text                                   null comment '生成的分析结论',
    chartStatus varchar(128) default 'wait'            not null comment 'wait-等待,running-生成中,succeed-成功生成,failed-生成失败',
    execMessage text                                   null comment '执行信息',
    userId      bigint                                 null comment '创建图标用户 id',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;


-- AI 问答助手表
create table if not exists ai_assistant
(
    id             bigint auto_increment comment 'id' primary key,
    questionName   varchar(256)                           null comment '问题名称',
    questionGoal   text                                   null comment '问题概述',
    questionResult text                                   null comment '问答结果',
    questionType   varchar(512)                           null comment '问题类型',
    questionStatus varchar(128) default 'wait'            not null default 'wait' comment 'wait-等待,running-生成中,succeed-成功生成,failed-生成失败',
    execMessage    text                                   null comment '执行信息',
    userId         bigint                                 null comment '创建用户 id',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除'
) comment 'AI 问答助手信息表' collate = utf8mb4_unicode_ci;


-- AI调用次数表
create table if not exists ai_frequency
(
    id              bigint auto_increment comment 'id' primary key,
    userId          bigint                             not null comment '用户 id',
    totalFrequency  bigint   default 0                 not null comment '总调用次数',
    remainFrequency int      default 5                 not null comment '剩余调用次数',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除'
) comment 'ai调用次数表' collate = utf8mb4_unicode_ci;

-- 订单表
create table if not exists ai_frequency_order
(
    id               bigint auto_increment comment 'id' primary key,
    userId           bigint                             not null comment '用户 id',
    purchaseQuantity bigint(0)                          NOT NULL COMMENT '购买数量',
    price            float(255, 2)                      NOT NULL COMMENT '单价',
    totalAmount      float(10, 2)                       NOT NULL COMMENT '交易金额',
    orderStatus      int(0)                             NOT NULL DEFAULT 0 COMMENT '交易状态【0->待付款；1->已完成；2->无效订单,3->删除订单】',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除'
) comment '次数订单表' collate = utf8mb4_unicode_ci;

-- 支付记录表
create table if not exists alipay_info
(
    id              bigint auto_increment comment 'id' primary key,
    userId          bigint                             not null comment '用户 id',
    alipayAccountNo varchar(512)                       not null comment '支付宝流水账号',
    alipayId        varchar(1024) comment '支付宝唯一id',
    orderId         bigint comment '订单id',
    totalAmount     float(10, 2)                       NOT NULL COMMENT '交易金额',
    payStatus       int(0)                             NOT NULL DEFAULT 0 COMMENT '交易状态【0->未支付；1->已完成；2->支付失败】',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '支付时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除'
) comment '次数订单表' collate = utf8mb4_unicode_ci;
