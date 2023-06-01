package edu.bupt.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 登录拦截器类，用于拦截未登录的请求,记录访问并重定向至error界面
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    final private boolean needLogin;

    /**
     * 实例化<code>LoginInterceptor</code>类
     *
     * @param needLogin 是否激活登录检测
     */
    public LoginInterceptor(boolean needLogin) {
        this.needLogin = needLogin;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!needLogin) return true;
//        System.out.println(needLogin);

        String uri = request.getRequestURI();

        Integer isLogin = (Integer) Objects.requireNonNullElse(request.getSession().getAttribute("login"), 0);
        String username = (String) Objects.requireNonNullElse(request.getSession().getAttribute("username"), "Guest");
        if (isLogin != 1) {
            System.out.println("redirection:" + uri);
            request.getRequestDispatcher("/error/403").forward(request, response);
            log.info("access check:[" + username + "] access denied ->" + uri);
            return false;
        }

        log.info("access check:[" + username + "] access accept ->" + uri);
        return true;
    }
}
