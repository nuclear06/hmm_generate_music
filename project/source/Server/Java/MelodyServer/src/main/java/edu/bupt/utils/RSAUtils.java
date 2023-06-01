package edu.bupt.utils;

/**
 * 定义了RSA加密解密的接口
 */
public interface RSAUtils {
    /**
     * 初始化RSA算法。
     */
    void initRSA();

    /**
     * 在指定路径生成密钥对
     *
     * @param path       密钥路径
     * @param pubKeyName 公钥文件名
     * @param prvKeyName 私钥文件名
     */
    void generateKeyPairs(String path, String pubKeyName, String prvKeyName);

    /**
     * 生成密钥对
     */
    void generateKeyPairs();

    /**
     * 加密数据
     * 可以加密超过理论加密上限的内容
     *
     * @param data 二进制明文
     * @return 二进制密文
     */
    byte[] encryptData(byte[] data);

    /**
     * base64编码二进制数据
     *
     * @param data 二进制数据
     * @return base64编码结果
     */
    String b64EncData(byte[] data);


    /**
     * 解密二进制数据
     * 可以解码超过理论解密上限的结果
     *
     * @param cipher 二进制密文
     * @return 二进制明文
     */
    byte[] decryptData(byte[] cipher);

    /**
     * 解密base64编码后的RSA密文
     *
     * @param cipher 编码密文
     * @return 二进制明文
     */
    byte[] b64DecData(String cipher);


    /**
     * 保存密钥到指定路径
     *
     * @param path     路径
     * @param fileName 文件名
     * @param key      密钥
     */
    void saveKey(String path, String fileName, byte[] key);

    /**
     * 从磁盘指定路径加载公钥私钥文件
     *
     * @param path       路径
     * @param pubKeyName 公钥文件名
     * @param prvKeyName 私钥文件名
     */
    void loadKeyPair(String path, String pubKeyName, String prvKeyName);


    /**
     * 获得Base64编码后的公钥
     *
     * @return 公钥
     */
    String getBase64PubKey();

    /**
     * 获得Base64编码后的私钥
     *
     * @return 私钥
     */
    String getBase64PrvKey();

}
