package com.campus.activity.config;

import com.campus.activity.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册登录拦截器
 * 对应软件设计 3.4：登录拦截器校验会话
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 需要登录才能访问的路径（后续活动、报名模块逐步加入）
                .addPathPatterns("/teacher/**", "/student/**", "/activities/**", "/enrollments/**", "/admin/**")
                // 放行路径：首页、静态资源、注册、登录、登出
                .excludePathPatterns("/", "/register", "/login", "/logout", "/css/**", "/js/**", "/images/**");
    }
}
