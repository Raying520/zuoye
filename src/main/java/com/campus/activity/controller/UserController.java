package com.campus.activity.controller;

import com.campus.activity.dto.LoginForm;
import com.campus.activity.dto.RegisterForm;
import com.campus.activity.dto.UserSession;
import com.campus.activity.interceptor.LoginInterceptor;
import com.campus.activity.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户控制器：注册（FR1）
 */
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 注册页面 */
    @GetMapping("/register")
    public String registerPage(RegisterForm form) {
        return "register";
    }

    /** 提交注册 */
    @PostMapping("/register")
    public String register(@Valid RegisterForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // 1. 表单校验失败：回到注册页显示字段错误
        if (bindingResult.hasErrors()) {
            return "register";
        }
        // 2. 业务校验（账号唯一、两次密码一致）
        try {
            userService.register(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("bizError", e.getMessage());
            return "register";
        }
        // 3. 注册成功，跳转到登录页
        redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
        return "redirect:/login";
    }

    /** 登录页面 */
    @GetMapping("/login")
    public String loginPage(LoginForm loginForm) {
        return "login";
    }

    /** 提交登录（FR2） */
    @PostMapping("/login")
    public String login(@Valid LoginForm loginForm,
                        BindingResult bindingResult,
                        Model model,
                        HttpSession session) {
        // 1. 表单校验失败：回到登录页显示字段错误
        if (bindingResult.hasErrors()) {
            return "login";
        }
        // 2. 业务校验（账号存在且密码匹配）
        UserSession userSession;
        try {
            userSession = userService.login(loginForm);
        } catch (IllegalArgumentException e) {
            model.addAttribute("bizError", e.getMessage());
            return "login";
        }
        // 3. 写入 Session，完成角色分流
        session.setAttribute(LoginInterceptor.SESSION_KEY, userSession);
        if (userSession.isTeacher()) {
            return "redirect:/teacher/home";
        }
        return "redirect:/student/home";
    }

    /** 登出 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
