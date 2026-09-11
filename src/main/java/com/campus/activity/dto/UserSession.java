package com.campus.activity.dto;

import com.campus.activity.entity.User;
import java.io.Serializable;

/**
 * 登录会话信息（写入 Session，避免直接存放 JPA 实体）
 * 对应软件设计 3.2：用户信息写入 Session
 */
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String name;
    private User.Role role;

    public static UserSession fromUser(User user) {
        UserSession session = new UserSession();
        session.id = user.getId();
        session.username = user.getUsername();
        session.name = user.getName();
        session.role = user.getRole();
        return session;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public User.Role getRole() { return role; }

    public void setRole(User.Role role) { this.role = role; }

    public boolean isTeacher() { return role == User.Role.TEACHER; }

    public boolean isStudent() { return role == User.Role.STUDENT; }

    /** 返回中文角色名，供页面显示 */
    public String getRoleName() {
        return role == User.Role.TEACHER ? "教师" : "学生";
    }
}
