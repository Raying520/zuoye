package com.campus.activity.controller;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.UserSession;
import com.campus.activity.interceptor.LoginInterceptor;
import com.campus.activity.service.ActivityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 活动控制器（FR3 发布、FR4 浏览、FR7 管理）
 */
@Controller
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /** 发布活动页面（FR3，仅教师） */
    @GetMapping("/new")
    public String newForm(ActivityForm activityForm, HttpSession session, Model model) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            model.addAttribute("bizError", "只有教师可以发布活动");
            return "login";
        }
        return "activity/form";
    }

    /** 提交发布活动（FR3） */
    @PostMapping
    public String publish(@Valid ActivityForm form,
                          BindingResult bindingResult,
                          HttpSession session,
                          Model model) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            model.addAttribute("bizError", "只有教师可以发布活动");
            return "login";
        }
        if (bindingResult.hasErrors()) {
            return "activity/form";
        }
        try {
            activityService.publish(form, user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("bizError", e.getMessage());
            return "activity/form";
        }
        return "redirect:/teacher/home";
    }

    /** 活动列表（FR4，登录用户均可浏览） */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("activities", activityService.findAll());
        return "activity/list";
    }

    /** 活动详情（FR4） */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var activity = activityService.findById(id);
        if (activity == null) {
            return "redirect:/activities";
        }
        model.addAttribute("activity", activity);
        return "activity/detail";
    }

    private UserSession currentUser(HttpSession session) {
        return (UserSession) session.getAttribute(LoginInterceptor.SESSION_KEY);
    }
}
