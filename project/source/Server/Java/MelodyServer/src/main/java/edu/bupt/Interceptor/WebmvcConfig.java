package edu.bupt.Interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Webmvc配置类,此处用于添加拦截器黑白名单
 */
@Configuration
public class WebmvcConfig implements WebMvcConfigurer {
    @Value("${service.debug.need-login:true}")
    private boolean needLogin;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(needLogin))
                .excludePathPatterns("/login", "/register", "/user/login", "/user",
                        "/static/**", "/img/**", "/css/**", "/js/**", "/captcha", "/favicon.ico", "/publickey",
                        "/error/**");
        // /login get 跳转登陆界面
        // /register get 跳转注册界面
        // /user/login post 登陆
        // /user get 是否存在 post 登陆
        // /static/** get 获得相关资源
        // /captcha get 获得验证码
        // /favicon.ico get 获得网站图标
        // /publickey get 获得rsa公钥
        // /error/** get 各个状态码的界面
    }
}
