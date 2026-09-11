package com.campus.activity.controller;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.UserSession;
import com.campus.activity.interceptor.LoginInterceptor;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.EnrollmentService;
import com.campus.activity.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 活动控制器（FR3 发布、FR4 浏览、FR7 管理）
 */
@Controller
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    public ActivityController(ActivityService activityService,
                              EnrollmentService enrollmentService,
                              UserService userService) {
        this.activityService = activityService;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
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

    /** 教师：我的活动管理列表（FR7） */
    @GetMapping("/manage")
    public String manage(HttpSession session, Model model) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            model.addAttribute("bizError", "只有教师可以管理活动");
            return "login";
        }
        model.addAttribute("items", activityService.findByTeacherWithEnrollmentCount(user.getId()));
        return "activity/manage";
    }

    /** 教师：取消活动（FR7 + BR5） */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            redirectAttributes.addFlashAttribute("bizError", "只有教师可以管理活动");
            return "redirect:/login";
        }
        try {
            activityService.cancel(id, user);
            redirectAttributes.addFlashAttribute("success", "活动已取消");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return "redirect:/activities/manage";
    }

    /** 教师：结束活动（FR7 + BR5） */
    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            redirectAttributes.addFlashAttribute("bizError", "只有教师可以管理活动");
            return "redirect:/login";
        }
        try {
            activityService.close(id, user);
            redirectAttributes.addFlashAttribute("success", "活动已结束");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return "redirect:/activities/manage";
    }

    /** 教师：查看活动报名名单（FR6） */
    @GetMapping("/{id}/enrollments")
    public String enrollments(@PathVariable Long id,
                              HttpSession session,
                              Model model) {
        UserSession user = currentUser(session);
        var activity = activityService.findById(id);
        if (activity == null || user == null || !user.isTeacher()
                || !activity.getTeacherId().equals(user.getId())) {
            model.addAttribute("bizError", "无权查看该活动的报名名单");
            return "login";
        }
        var enrollmentList = enrollmentService.findByActivity(id);
        var names = userService.findNamesByIds(
                enrollmentList.stream().map(e -> e.getUserId()).toList());
        model.addAttribute("activity", activity);
        model.addAttribute("enrollments", enrollmentList);
        model.addAttribute("names", names);
        return "activity/enrollments";
    }

    private UserSession currentUser(HttpSession session) {
        return (UserSession) session.getAttribute(LoginInterceptor.SESSION_KEY);
    }
}
