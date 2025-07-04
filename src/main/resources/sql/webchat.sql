-- 创建数据库
CREATE DATABASE IF NOT EXISTS webchat;
-- 使用数据库
USE webchat;

-- 删除表
-- DROP TABLE IF EXISTS users;
-- DROP TABLE IF EXISTS chat_messages;

-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER'
);

-- 创建聊天消息表
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(255),
    receiver VARCHAR(255),
    content TEXT,
    timestamp DATETIME,
    type VARCHAR(255)
);

-- 插入测试数据
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'ADMIN'),
('user1', 'password123', 'USER');