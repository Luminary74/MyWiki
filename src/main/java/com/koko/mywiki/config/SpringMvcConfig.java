package com.koko.mywiki.config;

import com.koko.mywiki.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    //    @Resource
//    LogInterceptor logInterceptor;//配置拦截器
    @Resource
    LoginInterceptor loginInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(logInterceptor)
//                .addPathPatterns("/**");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/test/**",
                        "/redis/**",
                        "/user/login",
                        "/category/all",
                        "/ebook/list",
                        "/doc/all/**",
                        "/doc/vote/**",
                        "/doc/find-content/**",
                        "/ebook-snapshot/**"

                );
    }
}
