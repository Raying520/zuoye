# 校园活动管理系统 V1.0

实验一：基于工程意图的软件迭代开发

## 技术栈

Java 17 · Spring Boot 3 · Thymeleaf · Spring Data JPA · H2 数据库 · Maven

## 如何运行

1. 准备 JDK 17 及以上版本和 Maven 3.9+
2. 运行方式三选一：
   - 用 IntelliJ IDEA 打开本项目（自动识别 pom.xml 并导入），运行 `CampusActivityApplication`
   - Windows 双击 `start.bat`
   - 命令行执行 `mvn spring-boot:run`
3. 浏览器访问 http://localhost:8080

> 更详细的步骤、环境检查、常见问题见 `docs/本地运行指南.md`。

数据保存在项目目录下的 `db/` 文件夹（已加入 .gitignore，不入库）；删除该文件夹后重启即可重置数据。

## 当前进度

- [x] M1 项目骨架：Spring Boot 启动、H2 配置、首页
- [x] FR1 用户注册：表单校验、账号唯一、BCrypt 哈希
- [x] FR2 登录与角色分流：Session 会话、登录拦截器、教师/学生主页分流
- [x] FR3 活动发布：教师填写活动信息并保存，状态 = OPEN，容量 ≥ 1
- [x] FR4 活动浏览：活动列表、活动详情，按时间倒序展示
- [x] FR5 活动报名/取消报名：三重校验 BR2/BR3/BR4，数据库唯一约束兜底
- [x] FR6 报名名单查看：教师查看自己活动的报名学生名单
- [x] FR7 活动管理：教师结束/取消自己发布的活动（BR5）
- [x] M5 验证收尾：S1–S6 验证记录表、实验报告素材清单
- [x] 纯前端演示版：`frontend/` 目录，HTML/CSS/JS 实现，无需后端即可运行

## 纯前端快速体验

如果暂时不想配置 Java 环境，可以直接打开 `frontend/index.html`：

1. 进入 `frontend/` 文件夹
2. 双击 `index.html`
3. 用演示账号登录即可体验完整流程

> 纯前端版本使用浏览器 `localStorage` 保存数据，关闭页面后数据不会丢失（同一浏览器内）。详见 `frontend/README.md`。

## 快速验证

1. 启动后访问 http://localhost:8080/
2. 注册一个教师账号和一个学生账号
3. 分别登录，观察是否进入不同的主页
4. 按 `docs/验证记录.md` 中的 S1–S6 逐项验证并填写实际结果

## 项目文档

| 文档 | 内容 |
|---|---|
| docs/需求分析.md | 业务背景、用户角色、候选需求 FR1–FR7、业务规则 BR1–BR5 |
| docs/工程意图.md | V1.0 目标、范围、约束、完成依据、迭代里程碑 M1–M5 |
| docs/软件设计.md | 模块划分、核心流程图（mermaid）、数据设计、可追踪性、设计调整记录 |
| docs/验证记录.md | S1–S6 验收场景验证表（本地运行后填写） |
| docs/实验报告素材清单.md | 报告各章节对应素材、代码位置、截图清单、Git 提交时间线 |
| docs/项目实现总览.md | 全部代码整合映射：FR/BR 与 Controller/Service/页面一一对应 |
| docs/代码分步整合.md | 按 M1/FR1–FR7 分步汇总每一步涉及的全部源码（含脚本可重新生成） |
| docs/问题与解决方案.md | 开发过程中遇到的技术问题、原因分析与经验总结 |
