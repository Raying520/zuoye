package com.campus.activity.controller;

import com.campus.activity.dto.RegisterForm;
import com.campus.activity.service.UserService;
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
        // 3. 注册成功（下一步实现登录后，跳转目标将改为 /login）
        redirectAttributes.addFlashAttribute("success", "注册成功，请牢记你的账号和密码");
        return "redirect:/register";
    }
}
