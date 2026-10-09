package com.campus.activity.controller;

import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.User;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.AdminService;
import com.campus.activity.service.EnrollmentService;
import com.campus.activity.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 管理员后台控制器（V3.0）
 * 提供用户管理、活动监管、操作日志、平台统计等功能。
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ActivityService activityService;
    private final EnrollmentService enrollmentService;
    private final AdminService adminService;

    public AdminController(UserService userService,
                           ActivityService activityService,
                           EnrollmentService enrollmentService,
                           AdminService adminService) {
        this.userService = userService;
        this.activityService = activityService;
        this.enrollmentService = enrollmentService;
        this.adminService = adminService;
    }

    /** 管理员首页 / 平台统计概览（FR21） */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";

        AdminService.PlatformStats stats = adminService.getStats();
        stats.userCount = userService.findAll().size();
        stats.activityCount = activityService.findAllForAdmin().size();
        stats.enrollmentCount = enrollmentService.findAll().size();
        stats.pendingEnrollmentCount = enrollmentService.countPending();
        stats.flaggedActivityCount = activityService.findAllForAdmin().stream()
                .filter(a -> a.getPlatformStatus() == Activity.PlatformStatus.FLAGGED).count();
        stats.suspendedActivityCount = activityService.findAllForAdmin().stream()
                .filter(a -> a.getPlatformStatus() == Activity.PlatformStatus.SUSPENDED).count();

        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    /** 用户管理列表（FR17） */
    @GetMapping("/users")
    public String users(Model model, HttpSession session) {
        if (currentAdmin(session) == null) return "redirect:/login";
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    /** 停用账号（FR16） */
    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id,
                              @RequestParam String reason,
                              HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";
        adminService.disableUser(id, reason, admin);
        return "redirect:/admin/users?success=账号已停用";
    }

    /** 启用账号（FR16） */
    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id,
                             @RequestParam String reason,
                             HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";
        adminService.enableUser(id, reason, admin);
        return "redirect:/admin/users?success=账号已启用";
    }

    /** 活动监管列表（FR18） */
    @GetMapping("/activities")
    public String activities(Model model, HttpSession session) {
        if (currentAdmin(session) == null) return "redirect:/login";
        model.addAttribute("activities", activityService.findAllForAdmin());
        return "admin/activities";
    }

    /** 标记活动待整改（FR19） */
    @PostMapping("/activities/{id}/flag")
    public String flagActivity(@PathVariable Long id,
                               @RequestParam String reason,
                               HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";
        adminService.flagActivity(id, reason, admin);
        return "redirect:/admin/activities?success=活动已标记待整改";
    }

    /** 下架活动（FR19） */
    @PostMapping("/activities/{id}/suspend")
    public String suspendActivity(@PathVariable Long id,
                                  @RequestParam String reason,
                                  HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";
        adminService.suspendActivity(id, reason, admin);
        return "redirect:/admin/activities?success=活动已下架";
    }

    /** 恢复活动（FR19） */
    @PostMapping("/activities/{id}/restore")
    public String restoreActivity(@PathVariable Long id,
                                  @RequestParam String reason,
                                  HttpSession session) {
        UserSession admin = currentAdmin(session);
        if (admin == null) return "redirect:/login";
        adminService.restoreActivity(id, reason, admin);
        return "redirect:/admin/activities?success=活动已恢复";
    }

    /** 操作日志（FR20） */
    @GetMapping("/logs")
    public String logs(Model model, HttpSession session) {
        if (currentAdmin(session) == null) return "redirect:/login";
        model.addAttribute("logs", adminService.findLogs());
        return "admin/logs";
    }

    private UserSession currentAdmin(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user");
        return (user != null && user.isAdmin()) ? user : null;
    }
}
