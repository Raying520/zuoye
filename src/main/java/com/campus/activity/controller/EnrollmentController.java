package com.campus.activity.controller;

import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
import com.campus.activity.interceptor.LoginInterceptor;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.EnrollmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * 报名控制器（FR5 报名/取消、FR6 名单查看、FR9 审核）
 */
@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final ActivityService activityService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                ActivityService activityService) {
        this.enrollmentService = enrollmentService;
        this.activityService = activityService;
    }

    /** 学生报名（FR5） */
    @PostMapping
    public String enroll(@RequestParam Long activityId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        try {
            enrollmentService.enroll(activityId, user);
            redirectAttributes.addFlashAttribute("success", "报名成功");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return "redirect:/activities/" + activityId;
    }

    /** 学生取消报名（FR5） */
    @PostMapping("/cancel")
    public String cancel(@RequestParam Long activityId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        try {
            enrollmentService.cancel(activityId, user);
            redirectAttributes.addFlashAttribute("success", "已取消报名");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return "redirect:/activities/" + activityId;
    }

    /** 教师：审核通过（FR9） */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        try {
            enrollmentService.approve(id, user);
            redirectAttributes.addFlashAttribute("success", "已通过报名审核");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return redirectToEnrollments(id);
    }

    /** 教师：审核拒绝（FR9） */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        UserSession user = currentUser(session);
        try {
            enrollmentService.reject(id, user);
            redirectAttributes.addFlashAttribute("success", "已拒绝报名");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("bizError", e.getMessage());
        }
        return redirectToEnrollments(id);
    }

    private String redirectToEnrollments(Long enrollmentId) {
        Enrollment enrollment = enrollmentService.findByIdHelper(enrollmentId);
        if (enrollment == null) {
            return "redirect:/activities/manage";
        }
        return "redirect:/activities/" + enrollment.getActivityId() + "/enrollments";
    }

    /** 我的报名（FR6，学生视角） */
    @GetMapping("/my")
    public String myEnrollments(HttpSession session, Model model) {
        UserSession user = currentUser(session);
        List<Enrollment> enrollments = enrollmentService.findByUser(user.getId());
        List<MyEnrollmentItem> items = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Activity activity = activityService.findById(e.getActivityId());
            if (activity != null) {
                items.add(new MyEnrollmentItem(e, activity));
            }
        }
        model.addAttribute("items", items);
        return "enrollment/my";
    }

    private UserSession currentUser(HttpSession session) {
        return (UserSession) session.getAttribute(LoginInterceptor.SESSION_KEY);
    }

    /** 我的报名列表项（报名记录 + 活动信息） */
    public record MyEnrollmentItem(Enrollment enrollment, Activity activity) {}
}
