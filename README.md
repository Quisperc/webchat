# WebChat10 —— 基于 Spring Boot 和 WebSocket 的实时聊天系统

## 项目简介

WebChat10 是一个基于 Spring Boot + WebSocket 的网页实时聊天系统，支持用户注册、登录、公共频道群聊、私聊、聊天记录持久化、管理员禁言/踢人等功能。前后端分离，后端负责业务逻辑和数据持久化，前端为原生 HTML/CSS/JS，支持现代浏览器。

## 主要功能

- 用户注册与登录
- 公共频道群聊
- 在线用户私聊
- 聊天记录持久化与查询
- 管理员禁言、踢人管理
- 实时在线用户列表

## 技术栈

- 后端：
  - Spring Boot 2.7.x
  - Spring WebSocket (STOMP 协议)
  - Spring Data JPA
  - MySQL 8.0+
  - Maven 3.6+
- 前端：
  - HTML5、CSS3、原生 JavaScript
  - SockJS、Stomp.js（通过 CDN 引入）

## 目录结构

```
webchat10/
├── pom.xml                        # Maven依赖配置
├── src/
│   ├── main/
│   │   ├── java/top/aoxc/webchat10/
│   │   │   ├── config/            # WebSocket配置
│   │   │   ├── controller/        # 控制器（API、WebSocket）
│   │   │   ├── listener/          # WebSocket事件监听
│   │   │   ├── model/             # 实体类
│   │   │   ├── repository/        # JPA持久层
│   │   │   └── Webchat10Application.java # 启动类
│   │   └── resources/
│   │       ├── application.yml    # 配置文件
│   │       ├── sql/webchat.sql    # 数据库脚本
│   │       └── static/
│   │           ├── index.html     # 前端主页面
│   │           ├── css/style.css  # 前端样式
│   │           └── js/            # （可扩展）前端脚本
│   └── test/
│       └── ...                    # 测试代码
└── ...
```

## 环境依赖

- JDK 1.8 及以上
- MySQL 8.0 及以上
- Maven 3.6 及以上
- 现代浏览器（Chrome、Edge、Firefox 等）

## 数据库初始化

1. 创建数据库：
   ```sql
   CREATE DATABASE webchat10 DEFAULT CHARACTER SET utf8mb4;
   ```
2. 可通过 JPA 自动建表，或手动执行如下 SQL：
   ```sql
   CREATE TABLE users (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     username VARCHAR(50) NOT NULL UNIQUE,
     password VARCHAR(100) NOT NULL,
     role VARCHAR(10) NOT NULL
   );
   CREATE TABLE chat_messages (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     sender VARCHAR(50) NOT NULL,
     receiver VARCHAR(50) NOT NULL,
     content TEXT NOT NULL,
     timestamp DATETIME NOT NULL,
     type VARCHAR(10) NOT NULL
   );
   ```
3. 配置数据库连接（`src/main/resources/application.yml`）：
   ```yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/webchat10?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
       username: <你的数据库用户名>
       password: <你的数据库密码>
   ```

## 启动与部署

1. 安装 JDK、MySQL、Maven，并配置环境变量。
2. 克隆本项目源码：
   ```bash
   git clone <你的仓库地址>
   cd webchat10
   ```
3. 配置数据库连接信息（见上）。
4. 使用 Maven 打包：
   ```bash
   mvn clean package
   ```
5. 运行后端服务：
   ```bash
   java -jar target/webchat10-*.jar
   ```
6. 前端静态页面（index.html）可直接通过浏览器访问，或部署到 Nginx 等 Web 服务器。
7. 客户端通过浏览器访问 `http://服务器IP:8080/` 即可使用。

## 前端访问与依赖

- 直接访问 `src/main/resources/static/index.html`，或后端启动后访问 `http://localhost:8080/`。
- 前端依赖通过 CDN 自动加载：
  - [SockJS](https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js)
  - [Stomp.js](https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js)
- 样式文件见 `css/style.css`。

## 核心文件说明

- `Webchat10Application.java`：Spring Boot 启动入口
- `WebSocketConfig.java`：WebSocket 与 STOMP 协议配置
- `UserController.java`：用户注册、登录、在线管理等 API
- `ChatController.java`：聊天消息收发、管理员操作等 WebSocket 处理
- `UserRepository.java`、`ChatMessageRepository.java`：JPA 数据持久化
- `User.java`、`ChatMessage.java`：实体类，映射数据库表
- `index.html`：前端主页面，含全部交互逻辑

## 常见问题

- **端口冲突**：如 8080 端口被占用，可在`application.yml`中修改端口。
- **数据库连接失败**：请检查数据库配置、账号密码及 MySQL 服务状态。
- **WebSocket 无法连接**：请确保后端服务已启动，浏览器支持 WebSocket。
- **管理员功能**：需注册账号后手动在数据库将用户`role`字段改为`ADMIN`。

## 联系方式

有问题或建议，请联系我~

---

> 本项目为《网络编程技术》课程设计作品，欢迎学习与交流。
