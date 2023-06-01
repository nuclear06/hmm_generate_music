package edu.bupt.utils.impl;

import edu.bupt.utils.RSAUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * The type Rsa utils.
 */
@Slf4j
@Component
public class RSAUtilsImpl implements RSAUtils {
    private final static String EncryptionMethod = "RSA";
    @Autowired
    private Environment env;

    /**
     * The Rsa path.
     */
    @Value("${rsa-key-path.path}")
    public String RSAPath;
    /**
     * The Public key name.
     */
    @Value("${rsa-key-path.public-key-name}")
    public String PublicKeyName;
    /**
     * The Private key name.
     */
    @Value("${rsa-key-path.private-key-name}")
    public String PrivateKeyName;

    /**
     * The constant ENC_PART_SIZE.
     */
    public static final int ENC_PART_SIZE = 245;
    /**
     * The constant DEC_PART_SIZE.
     */
    public static final int DEC_PART_SIZE = 256;

    private static final Base64.Decoder B64Decoder = Base64.getDecoder();
    private static final Base64.Encoder B64Encoder = Base64.getEncoder();
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    private static Cipher EncCipher;
    private static Cipher DecCipher;

    @Override
    @PostConstruct
    public void initRSA() {
        String pDir = RSAPath + "\\" + PublicKeyName;
        String sDir = RSAPath + "\\" + PrivateKeyName;

        File f1 = new File(pDir);
        File f2 = new File(sDir);
        if (!f1.exists() || !f2.exists()) {
            log.warn("RSA初始化->未检测到密钥,开始生成密钥");
            generateKeyPairs();
        } else {
            log.info("RSA初始化->检测到公钥文件,加载文件:" + pDir);
            log.info("RSA初始化->检测到私钥文件,加载文件:" + sDir);
        }
        loadKeyPair(RSAPath, PublicKeyName, PrivateKeyName);
        log.info("初始化程序结束");
    }

    @Override
    public void generateKeyPairs() {
        generateKeyPairs(null, null, null);
    }

    @Override
    public byte[] encryptData(byte[] originData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int total = originData.length;
            int offset = 0;
            while (total - offset > 0) {
                byte[] cache;
                if (total - offset > ENC_PART_SIZE) {
                    cache = EncCipher.doFinal(originData, offset, ENC_PART_SIZE);
                } else {
                    cache = EncCipher.doFinal(originData, offset, total - offset);
                }
                outputStream.write(cache);
                offset += ENC_PART_SIZE;
            }
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String b64EncData(byte[] data) {
        byte[] enc = encryptData(data);
        return new String(B64Encoder.encode(enc));
    }

    @Override
    public byte[] decryptData(byte[] originData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int total = originData.length;
            int offset = 0;
            while (total - offset > 0) {
                byte[] cache;
                if (total - offset > DEC_PART_SIZE) {
                    cache = DecCipher.doFinal(originData, offset, DEC_PART_SIZE);
                } else {
                    cache = DecCipher.doFinal(originData, offset, total - offset);
                }
                outputStream.write(cache);
                offset += DEC_PART_SIZE;
            }
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            log.error(String.valueOf(e));
//            throw new RuntimeException(e);
            return null;
        }
    }

    @Override
    public byte[] b64DecData(String cipher) {
        try {
            byte[] decode = B64Decoder.decode(cipher);
            return decryptData(decode);
        } catch (IllegalArgumentException e) {
            log.warn("传入数据无法进行base64解码");
            return null;
        }
    }

    @Override
    public void generateKeyPairs(String path, String pubKeyName, String prvKeyName) {
        path = path != null ? path : RSAPath;
        pubKeyName = pubKeyName != null ? pubKeyName : PublicKeyName;
        prvKeyName = prvKeyName != null ? path : PrivateKeyName;

        System.out.println(env);

        if (path == null || pubKeyName == null || prvKeyName == null) {
            log.error("未检测到RSA配置:" + path);
            throw new RuntimeException("未检测到RSA配置:" + path);
        }

        try {
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance(EncryptionMethod);
            kpGen.initialize(2048);
            KeyPair kp = kpGen.generateKeyPair();
            PublicKey pubKey = kp.getPublic();
            PrivateKey prvKey = kp.getPrivate();

//            System.out.println(pubKey);
//            System.out.println(prvKey);

            saveKey(path, pubKeyName, pubKey.getEncoded());
            saveKey(path, prvKeyName, prvKey.getEncoded());
            log.info("已生成RSA密钥对,路径:" + path);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void saveKey(String path, String fileName, byte[] key) {
        try (FileOutputStream stream = new FileOutputStream(path + "\\" + fileName)) {
            stream.write(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadKeyPair(String path, String pubKeyName, String prvKeyName) {
        File pubFile = new File(path + "\\" + pubKeyName);
        File prvFile = new File(path + "\\" + prvKeyName);
        if (!pubFile.exists() || !prvFile.exists()) {
            log.error("未找到公钥或私钥文件!");
            return;
        }


        try {
            byte[] pkData = new byte[(int) pubFile.length()];
            byte[] skData = new byte[(int) prvFile.length()];
            new FileInputStream(pubFile).read(pkData);
            new FileInputStream(prvFile).read(skData);

            KeyFactory kf = KeyFactory.getInstance(EncryptionMethod);
            // 恢复公钥:
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkData);
            publicKey = kf.generatePublic(pkSpec);
            // 恢复私钥:
            PKCS8EncodedKeySpec skSpec = new PKCS8EncodedKeySpec(skData);
            privateKey = kf.generatePrivate(skSpec);

            EncCipher = Cipher.getInstance(EncryptionMethod);
            EncCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            DecCipher = Cipher.getInstance(EncryptionMethod);
            DecCipher.init(Cipher.DECRYPT_MODE, privateKey);

            log.info("RSA密钥对加载成功,文件位置:" + path);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBase64PubKey() {
        return new String(B64Encoder.encode(publicKey.getEncoded()));
    }

    @Override
    public String getBase64PrvKey() {
        return new String(B64Encoder.encode(privateKey.getEncoded()));
    }
}
