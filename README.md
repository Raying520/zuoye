# 校园活动管理系统 V1.0

实验一：基于工程意图的软件迭代开发

## 技术栈

Java 17 · Spring Boot 3 · Thymeleaf · Spring Data JPA · H2 数据库 · Maven

## 如何运行

1. 准备 JDK 17 及以上版本
2. 运行方式二选一：
   - 用 IntelliJ IDEA 打开本项目（自动识别 pom.xml 并导入），运行 `CampusActivityApplication`
   - 命令行执行 `mvn spring-boot:run`
3. 浏览器访问 http://localhost:8080

数据保存在项目目录下的 `db/` 文件夹（已加入 .gitignore，不入库）；删除该文件夹后重启即可重置数据。

## 当前进度

- [x] M1 项目骨架：Spring Boot 启动、H2 配置、首页
- [x] FR1 用户注册：表单校验、账号唯一、BCrypt 哈希
- [x] FR2 登录与角色分流：Session 会话、登录拦截器、教师/学生主页分流
- [x] FR3 活动发布：教师填写活动信息并保存，状态 = OPEN，容量 ≥ 1
- [x] FR4 活动浏览：活动列表、活动详情，按时间倒序展示
- [x] FR5 活动报名/取消报名：三重校验 BR2/BR3/BR4，数据库唯一约束兜底
- [ ] FR6 报名名单查看（教师视角待开发）
- [ ] FR6 报名名单（待开发）
- [ ] FR7 活动管理（待开发）

## 快速验证

1. 启动后访问 http://localhost:8080/
2. 注册一个教师账号和一个学生账号
3. 分别登录，观察是否进入不同的主页
4. 直接访问 http://localhost:8080/teacher/home 或 http://localhost:8080/student/home（未登录时）应被拦截回登录页

## 项目文档

| 文档 | 内容 |
|---|---|
| docs/需求分析.md | 业务背景、用户角色、候选需求 FR1–FR7、业务规则 BR1–BR5 |
| docs/工程意图.md | V1.0 目标、范围、约束、完成依据、迭代里程碑 M1–M5 |
| docs/软件设计.md | 模块划分、核心流程图（mermaid）、数据设计、可追踪性 |
