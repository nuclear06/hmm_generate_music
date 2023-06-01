package edu.bupt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 当用户访问错误网址或无权访问页面时，提供错误界面url
 *
 * @author saniter
 * @date 2023/05/23
 */
@RequestMapping("/error")
@Tag(name = "ErrorController", description = "当用户访问错误网址或权限不足时用于返回错误网页")
@Controller
public class MyErrorController implements ErrorController {
    /**
     * 404页面
     *
     * @return 404.html
     */
    @RequestMapping
    @Operation(summary = "返回404网页")
    public String error404Page() {
        return "html/404";
    }

    /**
     * 403界面
     *
     * @return 403.html
     */
    @RequestMapping("/403")
    @Operation(summary = "返回403网页")
    public String error403Page() {
        return "html/403";
    }
}
