package edu.bupt.utils.impl;

import edu.bupt.utils.PwdSaltUtils;
import edu.bupt.utils.ServerBaseUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * The type Base utils.
 */
@Slf4j
@Component
public class ServerBaseUtilsImpl implements ServerBaseUtils {
    /**
     * The Favicon.
     */
    public static byte[] favicon;

    @Value("${cache.main-path}")
    private String mainCachePath;
    @Value("${cache.input-file-path}")
    private String inputFileCachePath;
    @Value("${cache.output-file-path}")
    private String outputFileCachePath;

    @PostConstruct
    private void init() {
        initFavicon();
        initCache();
    }

    @Override
    public void initFavicon() {
        loadFavicon("/favicon.ico");
    }

    @Override
    public void initCache() {
        if (mainCachePath == null) {
            log.error("主缓存目录未配置!");
            return;
        }
        if (inputFileCachePath == null) {
            inputFileCachePath = mainCachePath + "\\input-cache";
            log.warn("输入文件缓存路径未配置,使用默认配置:" + inputFileCachePath);
        }
        if (outputFileCachePath == null) {
            outputFileCachePath = mainCachePath + "\\output-cache";
            log.warn("输入文件缓存路径未配置,使用默认配置:" + outputFileCachePath);
        }

        if (createDir(mainCachePath)) {
            log.info("主缓存路径配置成功:" + mainCachePath);
        } else {
            log.error("主缓存路径文件创建失败!" + mainCachePath);
        }
        if (createDir(inputFileCachePath)) {
            log.info("输入文件缓存路径配置成功:" + inputFileCachePath);
        } else {
            log.error("输入文件缓存路径配置成功!" + inputFileCachePath);
        }
        if (createDir(outputFileCachePath)) {
            log.info("输出文件缓存路径配置成功:" + outputFileCachePath);
        } else {
            log.error("输出文件缓存路径配置成功!" + outputFileCachePath);
        }
    }

    @Override
    public byte[] getFavicon() {
        return favicon;
    }

    @Override
    public boolean captchaVerify(HttpSession session, String respCode) {
        String ans = (String) session.getAttribute("captcha");
        session.removeAttribute("captcha");
        if (ans == null) return false;
        return ans.equalsIgnoreCase(respCode);
    }

    @Override
    public String currentTime(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date data = new Date();
        return simpleDateFormat.format(data);
    }

    @Override
    public String getMainCachePath() {
        return mainCachePath;
    }

    @Override
    public String getInputCachePath() {
        return inputFileCachePath;
    }



    @Override
    public String getOutputCachePath() {
        return outputFileCachePath;
    }

    @Override
    public void loadFavicon(String path) {
        if (path == null || path.length() == 0) {
            return;
        }

        try {
            ClassPathResource img = new ClassPathResource(path);
            InputStream stream = img.getInputStream();

            int size = stream.available();
            favicon = new byte[size];
            int read = stream.read(favicon);
            log.info("favicon img load " + read + " bytes");

        } catch (IOException e) {
            log.warn("favicon img load failed!");
        }
    }

    /**
     * 创建文件夹
     *
     * @param path 路径
     * @return 成功或已存在返回true, 失败返回false
     */
    public static boolean createDir(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return true;
        } else if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
            return true;
        }
        return false;
    }

}
