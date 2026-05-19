CREATE DATABASE IF NOT EXISTS couple_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE couple_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt加密',
    nickname VARCHAR(32) COMMENT '昵称',
    avatar VARCHAR(512) COMMENT '头像URL',
    birthday DATE COMMENT '出生日期',
    city VARCHAR(64) COMMENT '当前城市',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude DECIMAL(10,7) COMMENT '经度',
    ip VARCHAR(45) COMMENT '最近登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS couples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL COMMENT '发起绑定方',
    user2_id BIGINT COMMENT '被绑定方(null=等待)',
    invite_code VARCHAR(8) NOT NULL UNIQUE COMMENT '邀请码',
    status TINYINT DEFAULT 0 COMMENT '0=等待 1=已绑定',
    bound_at DATETIME COMMENT '绑定时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情侣绑定表';

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '发布者',
    couple_id BIGINT NOT NULL COMMENT '所属情侣关系',
    content TEXT COMMENT '配文',
    post_type VARCHAR(16) NOT NULL COMMENT 'image/video',
    files JSON COMMENT '文件URL列表',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_couple (couple_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

CREATE TABLE IF NOT EXISTS memorials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    couple_id BIGINT NOT NULL COMMENT '所属情侣',
    user_id BIGINT NOT NULL COMMENT '创建者',
    title VARCHAR(64) NOT NULL COMMENT '纪念日名称',
    event_date DATE NOT NULL COMMENT '纪念日期',
    description VARCHAR(256) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_couple (couple_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纪念日表';

CREATE TABLE IF NOT EXISTS wishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    couple_id BIGINT NOT NULL COMMENT '所属情侣',
    user_id BIGINT NOT NULL COMMENT '创建者',
    title VARCHAR(64) NOT NULL COMMENT '心愿名称',
    description VARCHAR(512) COMMENT '描述',
    fulfilled TINYINT(1) DEFAULT 0 COMMENT '0=未实现 1=已实现',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_couple (couple_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿单表';
