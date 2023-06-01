package edu.bupt.utils;

import jakarta.servlet.http.HttpSession;
import lombok.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定义了服务器运行所需工具的接口。
 */
public interface ServerBaseUtils {

    /**
     * 加载favicon图像
     *
     * @param path 图像路径
     */
    void loadFavicon(String path);

    /**
     * 初始化加载favicon图像
     */
    void initFavicon();

    /**
     * 初始化缓存功能
     */
    void initCache();

    /**
     * 获取favicon数据
     *
     * @return 储存在<code>byte[]</code>中的图像数据
     */
    byte[] getFavicon();

    /**
     * 验证码校验
     *
     * @param session  {@link HttpSession}记录Http对话信息的类
     * @param respCode 响应的验证码
     * @return 验证码是否正确
     */
    boolean captchaVerify(HttpSession session, String respCode);

    /**
     * 获取当前时间
     *
     * @param format 返回时间格式
     * @return 时间
     */
    String currentTime(String format);

    /**
     * 获取主缓存目录路径
     *
     * @return 缓存路径
     */
    String getMainCachePath();

    /**
     * 获取输入缓存路径
     *
     * @return 缓存路径
     */
    String getInputCachePath();

    /**
     * 获取输出缓存路径
     *
     * @return 缓存路径
     */
    String getOutputCachePath();

    /**
     * 处理文件名以可在Header中使用。
     * 处理文件名，对于由英文字母、数字和-、_组成的原样返回。
     * 对于包含不在以上范围内字符的名字，计算md5后截取相同长度返回。
     *
     * @param fileName 原始文件名
     * @return 处理后的文件名
     */
    static String processRespFileName(@NonNull String fileName) {
        Matcher matcher = Pattern.compile("(\\[.*?]-\\[\\d+]-)(.*)").matcher(fileName);
        String name = fileName;
        String prefix = "";
        if (matcher.matches()) {
            prefix = matcher.group(1);
            name = matcher.group(2);
        }
        if (Pattern.matches("[a-zA-Z0-9-_%]*", name)) {
            return prefix + name;
        }
        int len = name.length() > 32 ? 32 : name.length();
        return prefix + PwdSaltUtils.md5Encrypt(name, null, null).substring(0, len);
    }
}
