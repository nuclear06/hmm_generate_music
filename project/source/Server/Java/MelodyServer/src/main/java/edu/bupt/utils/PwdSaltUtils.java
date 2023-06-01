package edu.bupt.utils;

import edu.bupt.domain.User;
import lombok.NonNull;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import java.util.Random;

/**
 * 实现了用户的密码加密，校验功能的类
 */
public class PwdSaltUtils {
    /**
     * Hash算法
     */
    private static final String HASH_METHOD = "MD5";
    /**
     * 盐长度
     */
    private static final int SALT_LENGTH = 32;
    /**
     * Hash计算轮数
     */
    private static final int HASH_EPOCH = 3;

    private static final Random random = new Random();
    /**
     * 盐字符池
     */
    private static final char[] SALT_CHARS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*" +
            "()").toCharArray();

    /**
     * 根据用户名生成盐
     *
     * @param user 用户信息
     * @return 随机的盐
     */
    public static String generateSalt(User user) {
        String username = user.getUsername();
        int nameLen = username.length();

        StringBuilder sb = new StringBuilder();
        sb.append(username);
        for (int i = 0; i < SALT_LENGTH - nameLen; i++) {
            char aChar = SALT_CHARS[random.nextInt(SALT_CHARS.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }

    /**
     * 根据用户id随机生成盐,修改<code>user</code>中的密码为加密后的密码
     *
     * @param user 用户信息
     * @return 盐
     */
    public static String encryptUserPwd(User user) {
        String salt = generateSalt(user);
        String result = md5Encrypt(user.getPassword(), salt, HASH_EPOCH);
        user.setPassword(result);
        return salt;
    }

    /**
     * md5加密字符串并返回加密结果
     *
     * @param from  明文
     * @param salt  盐
     * @param epoch 加密轮数
     * @return 密文
     */
    public static String md5Encrypt(@NonNull String from, String salt, Integer epoch) {
        if (salt != null && epoch != null) {
            ByteSource byteSalt = ByteSource.Util.bytes(salt);
            SimpleHash result = new SimpleHash(HASH_METHOD, from, byteSalt, epoch);
            return result.toString();
        } else {
            SimpleHash result = new SimpleHash(HASH_METHOD, from);
            return result.toString();
        }
    }

    /**
     * md5校验
     *
     * @param from 明文
     * @param salt 盐
     * @param to   密文
     * @return 是否匹配
     */
    public static boolean md5Verify(@NonNull String from, @NonNull String salt, @NonNull String to) {
        String res = md5Encrypt(from, salt, HASH_EPOCH);
        return res.equals(to);
    }

    public static void main(String[] args) {
        User user = new User("admin", "123456");
        System.out.println(user);
        String salt = encryptUserPwd(user);

        System.out.println(salt);
        System.out.println(user);
    }
}
