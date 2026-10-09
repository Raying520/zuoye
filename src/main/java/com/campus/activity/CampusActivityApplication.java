package com.campus.activity;

import com.campus.activity.entity.Enrollment;
import com.campus.activity.repository.EnrollmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

/**
 * 校园活动管理系统 V2.0 启动类
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
}
