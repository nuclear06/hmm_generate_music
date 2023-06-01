package edu.bupt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 提供与登陆服务相关的url
 *
 * @author saniter
 * @date 2023/05/23
 */
@Slf4j
@Controller
@Tag(name = "LoginController", description = "用于返回登陆界面，本身不实现登陆业务")
public class LoginController {

    /**
     * 注册服务url，返回注册界面
     *
     * @return register.html
     */
    @GetMapping("/register")
    @Operation(summary = "获取注册界面")
    public String register() {
        return "html/register";
    }

    /**
     * 登录服务url，返回登陆界面
     *
     * @return login.html
     */
    @GetMapping("/login")
    @Operation(summary = "获取登陆界面")
    public String login() {
        return "html/login";
    }
}
