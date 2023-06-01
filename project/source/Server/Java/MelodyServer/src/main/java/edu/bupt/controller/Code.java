package edu.bupt.controller;

/**
 * 定义了所有返回状态码的类
 *
 * @author saniter
 * @date 2023/05/24
 */
public class Code {
    /**
     * 成功
     */
    public static final Integer SUCCESS = 200;
    /**
     * 检验失败
     */
    public static final Integer VERIFY_FIL = 2001;
    /**
     * 验证码错误
     */
    public static final Integer CAPTCHA_ERR = 1001;
    /**
     * 用户已存在
     */
    public static final Integer USER_EXT = 4001;
    /**
     * 用户未存在
     */
    public static final Integer USER_NOT_EXT = 4000;
    /**
     * 登录成功
     */
    public static final Integer LOGIN_SUE = 5000;
    /**
     * 登录失败
     */
    public static final Integer LOGIN_FIL = 5001;
    /**
     * 注册成功
     */
    public static final Integer REGISTER_SUE = 6000;
    /**
     * 注册失败
     */
    public static final Integer REGISTER_FIL = 6001;
    /**
     * 用户级错误
     */
    public static final Integer BUSINESS_ERR = 9001;
    /**
     * 系统级错误
     */
    public static final Integer SYSTEM_ERR = 9003;
    /**
     * 业务级错误
     */
    public static final Integer SERVICE_ERR= 400;
}
