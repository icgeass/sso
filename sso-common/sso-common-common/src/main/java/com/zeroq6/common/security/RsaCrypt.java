
package com.zeroq6.common.security;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * Created by yuuki asuna on 2016/10/18.
 *
 * 参考：http://blog.csdn.net/chaijunkun/article/details/7275632/
 *
 */
public class RsaCrypt {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * KEY算法
     */
    private final static String KEY_ALGORITHM = "RSA";

    /**
     * 密文PKCS#1填充方式
     */
    public final static String CIPHER_PADDING_MODE_PKCS1 = "RSA/ECB/PKCS1Padding";

    /**
     * 密文无填充模式，"RSA" 就等同于 "RSA/ECB/NoPadding"
     */
    public final static String CIPHER_PADDING_MODE_NOPADDING = "RSA/ECB/NoPadding";


    /**
     * 私钥编码PKCS#1方式
     */
    public final static String PRIVATE_KEY_ENCODE_METHOD_PKCS1 = "PKCS#1";

    /**
     * 私钥编码PKCS#8方式
     */
    public final static String PRIVATE_KEY_ENCODE_METHOD_PKCS8 = "PKCS#8";


    private final static String PLAIN_TEXT_ENCODING_UTF8 = "utf-8";

    /**
     * 解密时默认该密文使用的密文填充方式
     */
    private String cipherDecryptPaddingMode = CIPHER_PADDING_MODE_NOPADDING;

    /**
     * 加密时密文的填充方式
     */
    private String cipherEncryptPaddingMode = CIPHER_PADDING_MODE_NOPADDING;

    /**
     * 私钥编码方式
     */
    private String privateKeyEncoding = PRIVATE_KEY_ENCODE_METHOD_PKCS8;


    /**
     * 加密的明文字符串编码方式
     */
    private String plainTextEncoding = PLAIN_TEXT_ENCODING_UTF8;

    /**
     * 私钥
     */
    private RSAPrivateKey privateKey;

    /**
     * 公钥
     */
    private RSAPublicKey publicKey;

    private String publicKeyBase64;

    private String privateKeyBase64;


    /**
     * 设置公私钥路径，
     */
    private String privateKeyPath;

    private String publicKeyPath;

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        try {
            loadPrivateKey(getClass().getResourceAsStream(privateKeyPath));
        } catch (Exception e) {
            try {
                loadPrivateKey(new FileInputStream(new File(privateKeyPath)));
            } catch (Exception ee) {
                logger.error("加载私钥失败, " + privateKeyPath, e);
                throw new RuntimeException(e);
            }
        }
        logger.info("加载私钥成功, " + privateKeyPath);
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
        try {
            loadPublicKey(getClass().getResourceAsStream(publicKeyPath));
        } catch (Exception e) {
            try {
                loadPublicKey(new FileInputStream(new File(publicKeyPath)));
            } catch (Exception ee) {
                logger.error("加载公钥失败, " + publicKeyPath, e);
                throw new RuntimeException(e);
            }
        }
        logger.info("加载公钥成功, " + publicKeyPath);
    }

    public RsaCrypt(){}

    /**
     * 得到新的实例
     * @return
     */
    public static RsaCrypt newInstance(){
        return new RsaCrypt();
    }


    /**
     * 获取私钥
     *
     * @return 当前的私钥对象
     */
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取公钥
     *
     * @return 当前的公钥对象
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }


    /**
     * 随机生成密钥对，秘钥长度1024
     */
    public RsaCrypt genKeyPair1024() throws Exception{
        return genKeyPair(1024);
    }

    /**
     * 随机生成密钥对，秘钥长度2048
     */
    public RsaCrypt genKeyPair2048() throws Exception {
        return genKeyPair(2048);
    }

    /**
     * 随机生成密钥对
     *
     * 该方法生成的私钥的编码方式为PKCS#8
     *
     * openssl genrsa -out rsa_private_key.pem 2048 ----> 私钥PKCS#1
     * openssl pkcs8 -topk8 -in rsa_private_key.pem -out pkcs8_rsa_private_key.pem -nocrypt  ---->将PKCS#1编码的私钥转换为PKCS#8方式
     * openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -pubout  ----> 从私钥中导出公钥
     */
    public RsaCrypt genKeyPair(int keyLength) throws Exception{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keyLength, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        return this;
    }


    /**
     * 输入流中加载公钥
     * @param in
     * @return
     * @throws Exception
     */
    public RsaCrypt loadPublicKey(InputStream in) throws Exception {
        return loadPublicKey(loadKey(in));
    }


    /**
     * 从字符串中加载公钥
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public RsaCrypt loadPublicKey(String publicKeyStr) throws Exception {
        byte[] buffer = Base64.getDecoder().decode(publicKeyStr);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        return this;
    }

    /**
     * 从输入流中加载私钥
     * @param in
     * @return
     * @throws Exception
     */
    public RsaCrypt loadPrivateKey(InputStream in) throws Exception {
        return loadPrivateKey(loadKey(in));
    }

    /**
     * 从字符串中加载私钥
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public RsaCrypt loadPrivateKey(String privateKeyStr) throws Exception {
        byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
        KeySpec keySpec = null;
        if(PRIVATE_KEY_ENCODE_METHOD_PKCS1.equals(privateKeyEncoding)){
            RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(buffer));
            keySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
        }else if(PRIVATE_KEY_ENCODE_METHOD_PKCS8.equals(privateKeyEncoding)){
            keySpec = new PKCS8EncodedKeySpec(buffer);
        }else{
            throw new RuntimeException("不支持的私钥编码方式, " + privateKeyEncoding);
        }
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        return this;
    }


    public String encryptToBase64String(String plainText) throws Exception{
        return Base64.getEncoder().encodeToString(encrypt(plainText.getBytes(plainTextEncoding)));
    }


    /**
     * 加密过程
     *
     * @param plainData 明文数据
     * @return
     * @throws Exception 加密过程中的异常信息
     */
    public byte[] encrypt(byte[] plainData) throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = Cipher.getInstance(cipherEncryptPaddingMode, new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainData);
    }


    public String decryptFromBase64String(String plainTextBase64) throws Exception{
        return new String(decrypt(Base64.getDecoder().decode(plainTextBase64)), plainTextEncoding);
    }

    /**
     * 解密过程
     *
     * @param cipherData 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public byte[] decrypt(byte[] cipherData) throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = Cipher.getInstance(cipherDecryptPaddingMode, new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(cipherData);
    }


    /**
     * 读取公钥或私钥
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static String loadKey(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null) {
            if (readLine.charAt(0) == '-') {
                continue;
            } else {
                sb.append(readLine);
            }
        }
        return sb.toString().trim();
    }



    /**
     * 获得私钥Base64
     * @return
     */
    public String getPrivateKeyBase64() {
        if(null == privateKeyBase64){
            privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }
        return privateKeyBase64;
    }

    /**
     * 获得公钥Base64
     * @return
     */
    public String getPublicKeyBase64() {
        if(null == publicKeyBase64){
            publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }
        return publicKeyBase64;
    }


    /**
     * 格式化公钥或私钥Base64字符串
     * @param key
     * @param isPrivateKey
     * @return
     */
    private String formatKey(String key, boolean isPrivateKey){
        String begin = "-----BEGIN PUBLIC KEY-----";
        String end = "-----END PUBLIC KEY-----";
        if(isPrivateKey){
            begin = "-----BEGIN PRIVATE KEY-----";
            end= "-----END PRIVATE KEY-----";
        }
        int len = key.length();
        String lineSeparator = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer(begin);
        sb.append(lineSeparator);
        for(int i= 0; i < len; i++){
            sb.append(key.charAt(i));
            if((i + 1) % 76 == 0){ // RFC2045编码后的字符串每行不得超过76个字符
                if(i + 1 != len){
                    sb.append(lineSeparator);
                }
            }
        }
        sb.append(lineSeparator);
        return sb.append(end).toString();

    }


    public RsaCrypt setCipherDecryptPaddingMode(String cipherDecryptPaddingMode) {
        this.cipherDecryptPaddingMode = cipherDecryptPaddingMode;
        return this;
    }

    public RsaCrypt setCipherEncryptPaddingMode(String cipherEncryptPaddingMode) {
        this.cipherEncryptPaddingMode = cipherEncryptPaddingMode;
        return this;
    }

    public RsaCrypt setPrivateKeyEncoding(String privateKeyEncoding) {
        this.privateKeyEncoding = privateKeyEncoding;
        return this;
    }

    public RsaCrypt setPlainTextEncoding(String plainTextEncoding) {
        this.plainTextEncoding = plainTextEncoding;
        return this;
    }

}
