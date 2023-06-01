package edu.bupt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.bupt.dao.HistoryDao;
import edu.bupt.domain.History;
import edu.bupt.domain.User;
import edu.bupt.domain.UserData;
import edu.bupt.service.IHistoryService;
import edu.bupt.service.IUserService;
import edu.bupt.utils.RSAUtils;
import edu.bupt.utils.ServerBaseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 提供与用户信息和用户状态有关的url
 *
 * @author saniter
 * @date 2023/05/23
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Tag(name = "UserController", description = "实现用户注册登陆所需功能的接口")
public class UserController {
    @Autowired
    private ServerBaseUtils serverBaseUtils;
    @Autowired
    private IUserService userService;

    @Autowired
    private RSAUtils rsaUtils;


    /**
     * 检测用户名是否已被占用
     *
     * @param username 用户名
     * @return {@link Result}通用返回类,包含状态码和信息
     * <ul>
     *     <li> {@code Code.BUSINESS_ERR=9001} 传入的用户名为{@code null}
     *     <li> {@code Code.USER_NOT_EXT=4000} 用户名未被使用
     *     <li> {@code Code.USER_EXT=4001} 用户名已被占用
     * </ul>
     */
    @GetMapping
    @Operation(summary = "检测用户名是否被占用")
    @ApiResponses({@ApiResponse(responseCode = "4000", description = "用户名可用"),
            @ApiResponse(responseCode = "4001", description = "用户名已被占用")})
    public Result exist(@Parameter(description = "需要检测的用户名") String username) {
        if (username == null) {
            return new Result(Code.BUSINESS_ERR, null, "网络错误,请稍后再试!");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userService.getOne(wrapper);
        boolean flag = user == null; //true说明不存在
        Integer code = flag ? Code.USER_NOT_EXT : Code.USER_EXT;
        String msg = flag ? "用户名可用!" : "用户名已被占用!";
        return new Result(code, msg);
    }

    /**
     * 提供用户登陆的api,接受POST请求
     *
     * @param user {@link User}记录用户信息的类,类中的用户名,密码,验证码不能为空
     * @param req  {@link HttpServletRequest}http请求相关信息和方法
     * @return {@link Result}通用返回类,包含状态码和信息
     * <ul>
     *     <li> {@code Code.LOGIN_SUE=5000} 登陆成功
     *     <li> {@code Code.LOGIN_FIL=5001} 登陆失败
     *     <li> {@code Code.CAPTCHA_ERR=1001} 验证码错误
     *     <li> {@code Code.BUSINESS_ERR=9001} 数据错误
     *     <li> {@code Code.VERIFY_FIL=2001} 密码格式错误
     * </ul>
     */
    @PostMapping("/login")
    @Operation(summary = "用户登陆请求")
    @ApiResponses({@ApiResponse(responseCode = "5000", description = "登陆成功"),
            @ApiResponse(responseCode = "5001", description = "登陆失败"),
            @ApiResponse(responseCode = "1001", description = "验证码错误")})
    public Result home(@Parameter(description = "要登录的用户信息") @RequestBody @Valid User user, HttpServletRequest req) {
        String username = user.getUsername();
        Result res;
        if ((res = verifyCaptchaAndDecPwd(user, req.getSession())) != null) {
            return res;
        }

        boolean flag = userService.login(user);//true说明登陆成功
        Integer code = flag ? Code.LOGIN_SUE : Code.LOGIN_FIL;
        String msg = flag ? "登陆成功!" : "用户名或密码错误!";

        if (flag) req.getSession().setAttribute("login", 1);
        if (flag) req.getSession().setAttribute("username", username);
        return new Result(code, msg);
    }

    /**
     * 用户注册url,接受POST请求
     *
     * @param user {@link User}记录用户信息的类,类中的用户名,密码,验证码不能为空
     * @param req  {@link HttpServletRequest}http请求相关信息和方法
     * @return {@link Result}通用返回类,包含状态码和信息
     * <ul>
     *     <li> {@code Code.REGISTER_SUE=6000} 注册成功
     *     <li> {@code Code.REGISTER_FIL=6001} 注册失败
     *     <li> {@code Code.CAPTCHA_ERR=1001} 验证码错误
     * </ul>
     */
    @PostMapping
    @Operation(summary = "用户注册", description = "用户名不能重复")
    @ApiResponses({@ApiResponse(responseCode = "6000", description = "注册成功"),
            @ApiResponse(responseCode = "6001", description = "注册失败"),
            @ApiResponse(responseCode = "1001", description = "验证码错误")})
    public Result register(@Parameter(description = "需要注册的用户信息") @RequestBody @Valid User user,
                           HttpServletRequest req) {
        String username = user.getUsername();
        Result res;
        if ((res = verifyCaptchaAndDecPwd(user, req.getSession())) != null) {
            return res;
        }

        boolean flag = userService.register(user);
        if (flag) {
            return new Result(Code.REGISTER_SUE, null, "注册成功!");
        } else {
            return new Result(Code.REGISTER_FIL, null, "注册失败!");
        }
    }

    /**
     * 清除用户当前登陆状态(退出登录)
     *
     * @param req {@link HttpServletRequest}http请求相关信息和方法
     * @return {@link Result}通用返回类,包含状态码和信息
     * {@code Code.SUCCESS=200} 退出成功
     */
    @Operation(summary = "用户退出登陆", description = "用户访问该链接后，服务端清除登陆信息")
    @ApiResponse(description = "退出登陆成功")
    @GetMapping("/exit")
    public Result exit(HttpServletRequest req) {
        HttpSession session = req.getSession();
        log.info(session.getAttribute("username") + " exit!");
        session.removeAttribute("login");
        session.removeAttribute("username");
        return new Result(Code.SUCCESS, "exit success!");
    }

    @Autowired
    private HistoryDao historyDao;
    @Autowired
    private IHistoryService historyService;

    /**
     * 获取用户个人数据
     *
     * @param req {@link HttpServletRequest}http请求相关信息和方法
     * @return {@link UserData}记录用户信息的类,类中记录了状态码和个人信息
     * <ul>
     *     <li> {@code Code.SERVICE_ERR=400} 请求错误,不附带信息
     *     <li> {@code Code.SUCCESS=200} 请求成功,附带个人信息
     * </ul>
     */
    @Operation(summary = "获得用户个人数据", description = "包括用户名，请求历史等")
    @GetMapping("/data")
    public UserData getUserData(HttpServletRequest req) {
        String username = (String) req.getSession().getAttribute("username");
        List<History> history = historyService.getHistory(username);
        if (history == null) {
            return new UserData(Code.SERVICE_ERR);
        }
        return new UserData(Code.SUCCESS, username, history);
    }

    /**
     * 验证验证码并base64解码密码,一切正常时返回null
     *
     * @param user    {@link User}记录用户信息的类
     * @param session {@link HttpSession}记录Http绘画信息的类
     * @return {@link Result}通用返回类,包含状态码和信息
     * <li> {@code Code.CAPTCHA_ERR=1001} 验证码错误
     * <li> {@code Code.BUSINESS_ERR=9001} 数据错误
     * <li> {@code Code.VERIFY_FIL=2001} 密码格式错误
     * <li> null 一切正常
     */
    private Result verifyCaptchaAndDecPwd(User user, HttpSession session) {
        if (!serverBaseUtils.captchaVerify(session, user.getCaptcha())) {
            return new Result(Code.CAPTCHA_ERR, "验证码错误!");
        }
        byte[] pwdDec;
        if ((pwdDec = rsaUtils.b64DecData(user.getPassword())) == null) {
            return new Result(Code.BUSINESS_ERR, "数据错误!");
        }
        String decPwd = new String(pwdDec);
        if (!Pattern.matches("^\\S{5,20}$", decPwd)) {
            return new Result(Code.VERIFY_FIL, "密码格式错误!");
        }
        user.setPassword(decPwd);
        return null;
    }
}
