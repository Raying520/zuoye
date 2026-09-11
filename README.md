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

## 项目文档

| 文档 | 内容 |
|---|---|
| docs/需求分析.md | 业务背景、用户角色、候选需求 FR1–FR7、业务规则 BR1–BR5 |
| docs/工程意图.md | V1.0 目标、范围、约束、完成依据、迭代里程碑 M1–M5 |
| docs/软件设计.md | 模块划分、核心流程图（mermaid）、数据设计、可追踪性 |
