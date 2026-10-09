package com.campus.activity.controller;

import com.campus.activity.dto.ActivityForm;
import com.campus.activity.dto.UserSession;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
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

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 活动控制器（FR3 发布、FR4 浏览、FR7 管理、FR13 编辑）
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
        model.addAttribute("edit", false);
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
            model.addAttribute("edit", false);
            return "activity/form";
        }
        try {
            activityService.publish(form, user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("bizError", e.getMessage());
            model.addAttribute("edit", false);
            return "activity/form";
        }
        return "redirect:/teacher/home";
    }

    /** 编辑活动页面（FR13，仅教师、仅 OPEN） */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {
        UserSession user = currentUser(session);
        Activity activity = activityService.findById(id);
        if (activity == null || user == null || !user.isTeacher()
                || !activity.getTeacherId().equals(user.getId())) {
            model.addAttribute("bizError", "无权编辑该活动");
            return "login";
        }
        if (activity.getStatus() != Activity.Status.OPEN) {
            model.addAttribute("bizError", "活动当前状态不可编辑");
            model.addAttribute("activity", activity);
            return "activity/detail";
        }

        ActivityForm form = new ActivityForm();
        form.setTitle(activity.getTitle());
        form.setDescription(activity.getDescription());
        form.setActivityTime(activity.getActivityTime());
        form.setPlace(activity.getPlace());
        form.setCapacity(activity.getCapacity());
        form.setRequirements(activity.getRequirements());
        form.setRegistrationDeadline(activity.getRegistrationDeadline());
        form.setRequireAudit(activity.getRequireAudit());
        form.setAllowWaitlist(activity.getAllowWaitlist());
        model.addAttribute("activityForm", form);
        model.addAttribute("activity", activity);
        model.addAttribute("edit", true);
        return "activity/form";
    }

    /** 提交编辑活动（FR13） */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid ActivityForm form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        UserSession user = currentUser(session);
        if (user == null || !user.isTeacher()) {
            model.addAttribute("bizError", "只有教师可以编辑活动");
            return "login";
        }
        Activity activity = activityService.findById(id);
        if (activity == null || !activity.getTeacherId().equals(user.getId())) {
            model.addAttribute("bizError", "无权编辑该活动");
            return "login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("activity", activity);
            model.addAttribute("edit", true);
            return "activity/form";
        }
        try {
            activityService.update(id, form, user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("bizError", e.getMessage());
            model.addAttribute("activity", activity);
            model.addAttribute("edit", true);
            return "activity/form";
        }
        return "redirect:/activities/manage";
    }

    /** 活动列表（FR4，登录用户均可浏览） */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", activityService.findAllWithEnrollmentCount());
        return "activity/list";
    }

    /** 活动详情（FR4 + FR5 报名入口 + FR12/FR14 状态可见） */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return "redirect:/activities";
        }
        model.addAttribute("activity", activity);
        model.addAttribute("approvedCount", enrollmentService.approvedCount(id));
        model.addAttribute("waitingCount", enrollmentService.waitingCount(id));
        model.addAttribute("deadlinePassed", LocalDateTime.now().isAfter(activity.getRegistrationDeadline()));

        UserSession user = currentUser(session);
        if (user != null && user.isStudent()) {
            model.addAttribute("currentUser", user);
            Optional<Enrollment> myEnrollment = enrollmentService.findByActivityAndUser(id, user.getId());
            model.addAttribute("myEnrollment", myEnrollment.orElse(null));
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
        Activity activity = activityService.findById(id);
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
        model.addAttribute("approvedCount", enrollmentService.approvedCount(id));
        model.addAttribute("waitingCount", enrollmentService.waitingCount(id));
        return "activity/enrollments";
    }

    private UserSession currentUser(HttpSession session) {
        return (UserSession) session.getAttribute(LoginInterceptor.SESSION_KEY);
    }
}
