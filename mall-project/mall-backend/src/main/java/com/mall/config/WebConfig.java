package com.mall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Autowired
    private ContentCheckInterceptor contentCheckInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/admin/login",
                        "/assistant/**",
                        "/error"
                );
        
        // 添加内容检测拦截器
        registry.addInterceptor(contentCheckInterceptor)
                .addPathPatterns("/assistant/chat", "/assistant/chat/stream")
                .addPathPatterns("/comment/**")
                .addPathPatterns("/admin/announcement/**");
    }
}

