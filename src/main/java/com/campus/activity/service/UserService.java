package com.campus.activity.service;

import com.campus.activity.dto.RegisterForm;
import com.campus.activity.entity.User;
import com.campus.activity.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务：业务规则集中层
 * 注册流程（对应软件设计 3.1）：
 * 填写信息 → 选择角色 → 账号唯一性检查 → 密码 BCrypt 哈希 → 入库
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 注册新用户（FR1）
     *
     * @throws IllegalArgumentException 账号已存在或两次密码不一致
     */
    public void register(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("该账号已被注册");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        // 密码不明文入库（安全约束）
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setName(form.getName());
        user.setRole(form.getRole());
        userRepository.save(user);
    }
}
