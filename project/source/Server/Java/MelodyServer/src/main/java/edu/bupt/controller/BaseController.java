package edu.bupt.controller;

import edu.bupt.captcha.GenerateCaptcha;
import edu.bupt.utils.RSAUtils;
import edu.bupt.utils.ServerBaseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * 提供访问网页和验证等所需的服务url，不包含需要登录后才能使用的服务.
 */
@Slf4j
@Controller
@Tag(name = "BaseController", description = "提供网站访问的部分基础功能，可以在未登录的情况下访问")
public class BaseController {
    @Autowired
    private ServerBaseUtils serverBaseUtils;
    @Autowired
    private RSAUtils rsaUtils;

    /**
     * 提供获取网页favicon图像的url.
     *
     * @param response {@link HttpServletResponse}http响应相关信息和方法
     */
    @Operation(summary = "获取网站图标")
    @ApiResponse(description = "网页ico的二进制流")
    @GetMapping("/favicon.ico")
    public void favicon(HttpServletResponse response) {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            byte[] favicon = serverBaseUtils.getFavicon();
            outputStream.write(favicon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提供获取RSA算法公钥的url.
     *
     * @return {@link Result}通用返回类,包含状态码,数据和信息
     * {@code Code.SUCCESS=200} 请求成功
     */
    @GetMapping("/publickey")
    @ResponseBody
    @Operation(summary = "获取RSA公钥", description = "具体算法为RSA2048")
    @ApiResponse(responseCode = "200", description = "通用返回格式，data为没有头尾标记信息的PEM格式")
    public Result getPuvKey() {
        return new Result(Code.SUCCESS, rsaUtils.getBase64PubKey(), "GET SUCCESS!");
    }

    /**
     * 提供获取随机验证码图像的url.
     *
     * @param req  {@link HttpServletRequest}http请求相关信息和方法
     * @param resp {@link HttpServletResponse}http响应相关信息和方法
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码图片", description = "6位数字与字母的随机组合")
    @ApiResponse(description = "返回验证码图片的二进制流")
    public void getCaptcha(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();

        try (ServletOutputStream stream = resp.getOutputStream()) {
            String code = GenerateCaptcha.outputVerifyImage(1100, 300, stream, 6);
            session.setAttribute("captcha", code);
        } catch (IOException e) {
            log.warn(String.valueOf(e));
        }
    }

    /**
     * 提供访问网站根目录界面的url.
     *
     * @return 返回index.html
     */
    @GetMapping("/")
    @Operation(summary = "访问默认界面", description = "页面会自动跳转")
    public String defaultPage() {
        return "html/index";
    }

}
