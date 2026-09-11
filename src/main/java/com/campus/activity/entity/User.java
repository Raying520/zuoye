package com.campus.activity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 用户实体（对应数据表 app_user，逻辑名"用户表"）
 * 角色：STUDENT 学生 / TEACHER 教师
 */
@Entity
@Table(name = "app_user")
public class User {

    /** 用户角色 */
    public enum Role { STUDENT, TEACHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号，唯一 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 哈希后的密码，不存明文 */
    @Column(nullable = false)
    private String password;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 角色：STUDENT / TEACHER */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }
}
