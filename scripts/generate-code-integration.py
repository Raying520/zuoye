#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成分步代码整合文档：按 M1/FR1-FR7 汇总相关源码，便于实验报告引用。
"""
import os
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
SRC = BASE / "src"
OUT = BASE / "docs" / "代码分步整合.md"

# 每个步骤对应的文件（相对 src 根目录）
STEPS = [
    ("M1 项目骨架", [
        "pom.xml",
        "main/java/com/campus/activity/CampusActivityApplication.java",
        "main/resources/application.yml",
        "main/java/com/campus/activity/controller/HomeController.java",
        "main/resources/templates/index.html",
        "main/resources/static/css/style.css",
    ]),
    ("FR1 用户注册", [
        "main/java/com/campus/activity/entity/User.java",
        "main/java/com/campus/activity/repository/UserRepository.java",
        "main/java/com/campus/activity/dto/RegisterForm.java",
        "main/java/com/campus/activity/service/UserService.java",
        "main/java/com/campus/activity/controller/UserController.java",
        "main/resources/templates/register.html",
    ]),
    ("FR2 登录与角色分流", [
        "main/java/com/campus/activity/dto/LoginForm.java",
        "main/java/com/campus/activity/dto/UserSession.java",
        "main/java/com/campus/activity/interceptor/LoginInterceptor.java",
        "main/java/com/campus/activity/config/WebConfig.java",
        "main/resources/templates/login.html",
        "main/resources/templates/teacher/home.html",
        "main/resources/templates/student/home.html",
    ]),
    ("FR3 活动发布", [
        "main/java/com/campus/activity/entity/Activity.java",
        "main/java/com/campus/activity/repository/ActivityRepository.java",
        "main/java/com/campus/activity/dto/ActivityForm.java",
        "main/java/com/campus/activity/service/ActivityService.java",
        "main/resources/templates/activity/form.html",
    ]),
    ("FR4 活动浏览", [
        "main/java/com/campus/activity/dto/ActivityListItem.java",
        "main/resources/templates/activity/list.html",
        "main/resources/templates/activity/detail.html",
    ]),
    ("FR5 活动报名/取消报名", [
        "main/java/com/campus/activity/entity/Enrollment.java",
        "main/java/com/campus/activity/repository/EnrollmentRepository.java",
        "main/java/com/campus/activity/service/EnrollmentService.java",
        "main/java/com/campus/activity/controller/EnrollmentController.java",
        "main/resources/templates/enrollment/my.html",
    ]),
    ("FR6 + FR7 名单查看与活动管理", [
        "main/resources/templates/activity/manage.html",
        "main/resources/templates/activity/enrollments.html",
    ]),
    ("跨步骤复用控制器", [
        "main/java/com/campus/activity/controller/UserController.java",
        "main/java/com/campus/activity/controller/ActivityController.java",
    ]),
]

# 文件扩展名到 markdown 语言标识的映射
LANG_MAP = {
    ".java": "java",
    ".html": "html",
    ".yml": "yaml",
    ".xml": "xml",
    ".css": "css",
}


def lang_for(path: str) -> str:
    ext = Path(path).suffix.lower()
    return LANG_MAP.get(ext, "text")


def main():
    lines = [
        "# 校园活动管理系统 V1.0 — 代码分步整合\n",
        "> 本文档按开发步骤（M1 骨架 → FR1–FR7）汇总每一步涉及的关键源码，便于实验报告引用与小组整体回顾。\n",
        "> 注意：Controller 层中跨步骤复用的方法（如 `ActivityController` 同时包含 FR3/FR4/FR6/FR7 方法）在对应步骤中只列核心部分，完整代码请见源码文件。\n",
        "",
    ]

    for step_title, files in STEPS:
        lines.append(f"## {step_title}\n")
        for rel in files:
            full = SRC / rel if rel.startswith("main/") else BASE / rel
            if not full.exists():
                lines.append(f"\n> ⚠️ 文件不存在：{rel}\n")
                continue
            # 使用项目相对路径作为标题
            display = rel.replace("../", "")
            lines.append(f"\n### `{display}`\n")
            lines.append(f"```{lang_for(display)}")
            try:
                content = full.read_text(encoding="utf-8")
                lines.append(content.rstrip("\n"))
            except Exception as e:
                lines.append(f"读取失败：{e}")
            lines.append("\n```\n")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"已生成：{OUT}")


if __name__ == "__main__":
    main()
