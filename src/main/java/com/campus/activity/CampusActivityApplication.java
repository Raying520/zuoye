package com.campus.activity;

import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Enrollment;
import com.campus.activity.entity.User;
import com.campus.activity.repository.ActivityRepository;
import com.campus.activity.repository.EnrollmentRepository;
import com.campus.activity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * 校园活动管理系统 V3.0 启动类
 */
@SpringBootApplication
@EnableJpaAuditing
public class CampusActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusActivityApplication.class, args);
    }

    /**
     * V1 -> V2 数据兼容：把旧数据中 status 为 null 的报名记录标记为 APPROVED。
     * 这样既能保留历史报名记录，又能让新状态规则正常生效。
     */
    @Bean
    CommandLineRunner migrateV1Enrollments(EnrollmentRepository enrollmentRepository) {
        return args -> {
            List<Enrollment> legacy = enrollmentRepository.findAll().stream()
                    .filter(e -> e.getStatus() == null)
                    .toList();
            if (!legacy.isEmpty()) {
                for (Enrollment e : legacy) {
                    e.setStatus(Enrollment.Status.APPROVED);
                }
                enrollmentRepository.saveAll(legacy);
            }
        };
    }

    /**
     * V2 -> V3 数据兼容：旧用户缺少 status 字段时默认 ENABLED；
     * 旧活动缺少 platform_status 字段时默认 NORMAL。
     */
    @Bean
    CommandLineRunner migrateV2ToV3(UserRepository userRepository,
                                    ActivityRepository activityRepository) {
        return args -> {
            List<User> legacyUsers = userRepository.findAll().stream()
                    .filter(u -> u.getStatus() == null)
                    .toList();
            if (!legacyUsers.isEmpty()) {
                for (User u : legacyUsers) {
                    u.setStatus(User.Status.ENABLED);
                }
                userRepository.saveAll(legacyUsers);
            }

            List<Activity> legacyActivities = activityRepository.findAll().stream()
                    .filter(a -> a.getPlatformStatus() == null)
                    .toList();
            if (!legacyActivities.isEmpty()) {
                for (Activity a : legacyActivities) {
                    a.setPlatformStatus(Activity.PlatformStatus.NORMAL);
                }
                activityRepository.saveAll(legacyActivities);
            }
        };
    }

    /**
     * V3.0 数据兼容：预置系统管理员账号 admin / admin123（如果不存在）。
     * 管理员不通过注册入口创建，避免普通用户提升权限。
     */
    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
                admin.setName("系统管理员");
                admin.setRole(User.Role.ADMIN);
                admin.setStatus(User.Status.ENABLED);
                userRepository.save(admin);
            }
        };
    }
}
