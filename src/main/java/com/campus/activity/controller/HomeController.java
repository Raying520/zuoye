package com.campus.activity.controller;

import com.campus.activity.dto.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页与角色主页（对应软件设计 3.2：登录后角色分流）
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute(com.campus.activity.interceptor.LoginInterceptor.SESSION_KEY);
        model.addAttribute("currentUser", user);
        return "index";
    }

    /** 教师主页（FR2） */
    @GetMapping("/teacher/home")
    public String teacherHome(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute(com.campus.activity.interceptor.LoginInterceptor.SESSION_KEY);
        model.addAttribute("currentUser", user);
        return "teacher/home";
    }

    /** 学生主页（FR2） */
    @GetMapping("/student/home")
    public String studentHome(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute(com.campus.activity.interceptor.LoginInterceptor.SESSION_KEY);
        model.addAttribute("currentUser", user);
        return "student/home";
    }
}
