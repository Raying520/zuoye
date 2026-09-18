# 校园活动管理系统 — 纯前端演示版

这是基于 HTML + CSS + JavaScript 实现的独立前端版本，不依赖后端服务器，双击 HTML 文件即可在浏览器中运行。

## 技术栈

- HTML5
- CSS3
- JavaScript（原生，ES6+）
- localStorage / sessionStorage（本地数据持久化）

## 如何运行

1. 用浏览器直接打开 `index.html`
2. 或在本地文件管理器中双击任意 HTML 页面

无需安装 Node.js、Java 或任何构建工具。

## 演示账号

| 角色 | 账号 | 密码 | 姓名 |
|---|---|---|---|
| 教师 | teacher01 | 123456 | 王老师 |
| 学生 | student01 | 123456 | 张同学 |
| 学生 | student02 | 123456 | 李同学 |

## 功能清单

| 页面 | 路径 | 说明 |
|---|---|---|
| 首页 | index.html | 系统入口与功能介绍 |
| 登录 | login.html | 账号密码登录，按角色分流 |
| 注册 | register.html | 新用户注册，校验账号唯一性 |
| 教师工作台 | teacher-home.html | 教师概览与快捷入口 |
| 学生主页 | student-home.html | 学生概览与最新活动 |
| 活动列表 | activities.html | 按时间倒序展示所有活动 |
| 活动详情 | activity-detail.html?id=1 | 活动详情，学生可报名/取消 |
| 发布活动 | activity-form.html | 教师填写并发布新活动 |
| 活动管理 | activity-manage.html | 教师结束/取消自己发布的活动 |
| 报名名单 | activity-enrollments.html?id=1 | 教师查看某活动报名学生名单 |
| 我的报名 | my-enrollments.html | 学生查看自己的报名记录 |

## 数据说明

- 用户、活动、报名数据保存在浏览器 `localStorage` 中
- 登录状态保存在 `sessionStorage` 中，关闭浏览器标签后需重新登录
- 如需重置为初始演示数据，在浏览器控制台执行：`resetData()`

## 与后端项目的对应关系

本前端版本与 `../src/main/` 下的 Spring Boot 后端项目功能一一对应：

| 后端需求 | 前端实现 |
|---|---|
| FR1 用户注册 | register.html + data.js |
| FR2 登录与角色分流 | login.html + app.js |
| FR3 活动发布 | activity-form.html |
| FR4 活动浏览 | activities.html / activity-detail.html |
| FR5 活动报名/取消 | activity-detail.html + data.js |
| FR6 报名名单查看 | activity-enrollments.html |
| FR7 活动管理 | activity-manage.html |
| BR1–BR5 业务规则 | data.js 中的校验逻辑 |
