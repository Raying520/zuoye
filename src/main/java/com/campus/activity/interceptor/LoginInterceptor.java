package com.campus.activity.interceptor;

import com.campus.activity.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器（对应软件设计 3.4：登录拦截器校验会话）
 * 未登录用户访问受保护路径时，跳转登录页。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        UserSession user = session == null ? null : (UserSession) session.getAttribute(SESSION_KEY);
        if (user == null) {
            // 保存当前请求地址，登录成功后可根据需要跳转回来（V1.0 先统一跳首页）
            response.sendRedirect("/login");
            return false;
        }
        // 把当前用户附加到请求属性，方便 Controller 直接读取
        request.setAttribute(SESSION_KEY, user);
        return true;
    }
}
