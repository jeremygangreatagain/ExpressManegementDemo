-- 用户表
CREATE TABLE sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（MD5加盐）',
    phone VARCHAR(11) COMMENT '手机号',
    role ENUM('admin', 'staff', 'customer') NOT NULL COMMENT '角色类型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    store_id BIGINT COMMENT '所属门店ID（仅店员）',
    last_login TIMESTAMP COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 门店表
CREATE TABLE store (
    store_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '门店ID',
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address JSON NOT NULL COMMENT '地址信息（省市区详细地址）',
    gps_location POINT NOT NULL COMMENT '门店GPS坐标',
    status TINYINT DEFAULT 1 COMMENT '状态：0-关闭，1-营业',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE express_order (
    order_id BIGINT PRIMARY KEY COMMENT '订单号（雪花算法）',
    sender_info JSON NOT NULL COMMENT '寄件人信息（姓名、电话、地址）',
    receiver_info JSON NOT NULL COMMENT '收件人信息（姓名、电话、地址）',
    item_type ENUM('电器', '玻璃', '陶瓷') NOT NULL COMMENT '物品类型',
    current_store_id BIGINT COMMENT '当前处理门店ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-已创建，1-已揽收，2-运输中，3-派送中，4-已签收',
    created_by BIGINT NOT NULL COMMENT '创建用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (current_store_id) REFERENCES store(store_id),
    FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
);

-- 订单状态变更记录表
CREATE TABLE order_status_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    store_id BIGINT COMMENT '处理门店ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES express_order(order_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (operator_id) REFERENCES sys_user(user_id)
);

-- 操作日志表（按月分区）
CREATE TABLE operation_log (
    log_id BIGINT AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operation_type ENUM('创建订单', '更新状态', '删除订单', '用户管理', '系统设置') NOT NULL,
    target_id BIGINT COMMENT '操作对象ID',
    detail TEXT COMMENT '操作详情（JSON格式）',
    ip_address VARCHAR(50) COMMENT '操作IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    partition_date DATE GENERATED ALWAYS AS (DATE(created_at)) STORED,
    PRIMARY KEY (log_id, partition_date)
) PARTITION BY RANGE (TO_DAYS(partition_date)) (
    PARTITION p1 VALUES LESS THAN (TO_DAYS('2023-02-01')),
    PARTITION p2 VALUES LESS THAN (TO_DAYS('2023-03-01')),
    PARTITION p3 VALUES LESS THAN (TO_DAYS('2023-04-01')),
    PARTITION p4 VALUES LESS THAN (TO_DAYS('2023-05-01')),
    PARTITION p5 VALUES LESS THAN (TO_DAYS('2023-06-01')),
    PARTITION p6 VALUES LESS THAN (TO_DAYS('2023-07-01')),
    PARTITION p7 VALUES LESS THAN (TO_DAYS('2023-08-01')),
    PARTITION p8 VALUES LESS THAN (TO_DAYS('2023-09-01')),
    PARTITION p9 VALUES LESS THAN (TO_DAYS('2023-10-01')),
    PARTITION p10 VALUES LESS THAN (TO_DAYS('2023-11-01')),
    PARTITION p11 VALUES LESS THAN (TO_DAYS('2023-12-01')),
    PARTITION p12 VALUES LESS THAN (MAXVALUE)
);


-- 员工表
CREATE TABLE sys_staff (
    staff_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    phone VARCHAR(11) COMMENT '手机号',
    store_id BIGINT COMMENT '所属门店ID',
    position VARCHAR(50) COMMENT '职位',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    user_id BIGINT NOT NULL COMMENT '关联的用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
);

-- 索引
CREATE INDEX idx_order_status ON express_order(status);
CREATE INDEX idx_order_created_at ON express_order(created_at);
CREATE INDEX idx_store_gps ON store(gps_location);
CREATE INDEX idx_operation_log_type ON operation_log(operation_type);
CREATE INDEX idx_operation_log_date ON operation_log(created_at);
CREATE INDEX idx_staff_store ON sys_staff(store_id);
CREATE INDEX idx_staff_user ON sys_staff(user_id);