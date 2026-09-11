package com.campus.activity.repository;

import com.campus.activity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问接口（Spring Data JPA 自动实现）
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 注册时校验账号唯一（FR1） */
    boolean existsByUsername(String username);

    /** 登录时按账号查询用户（FR2） */
    Optional<User> findByUsername(String username);
}
