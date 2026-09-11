package com.campus.activity.controller;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.UserSession;
import com.campus.activity.interceptor.LoginInterceptor;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.EnrollmentService;
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
    private final EnrollmentService enrollmentService;

    public ActivityController(ActivityService activityService,
                              EnrollmentService enrollmentService) {
        this.activityService = activityService;
        this.enrollmentService = enrollmentService;
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
        model.addAttribute("items", activityService.findAllWithEnrollmentCount());
        return "activity/list";
    }

    /** 活动详情（FR4 + FR5 报名入口） */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model) {
        var activity = activityService.findById(id);
        if (activity == null) {
            return "redirect:/activities";
        }
        model.addAttribute("activity", activity);

        long enrolledCount = enrollmentService.countByActivity(id);
        model.addAttribute("enrolledCount", enrolledCount);

        UserSession user = currentUser(session);
        if (user != null && user.isStudent()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("isEnrolled", enrollmentService.isEnrolled(id, user.getId()));
        }
        return "activity/detail";
    }

    private UserSession currentUser(HttpSession session) {
        return (UserSession) session.getAttribute(LoginInterceptor.SESSION_KEY);
    }
}
