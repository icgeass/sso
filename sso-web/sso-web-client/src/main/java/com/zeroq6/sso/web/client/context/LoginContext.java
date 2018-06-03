package com.zeroq6.sso.web.client.context;
/**
 * Created by icgeass on 2017/2/22.
 */

import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.security.AesCrypt;
import com.zeroq6.sso.web.client.security.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 这个context主要是从cookie中获取的数据，记录登录相关信息，不包含用户详细信息
 */
public class LoginContext {

    private final static Logger logger = LoggerFactory.getLogger(LoginContext.class);

    private final static int LOGIN_CONTEXT_CACHE_SIZE = 2000;

    private final static int GROUP_ID_AES_CRYPT_SIZE = 2000;

    /**
     * ticket正则
     */
    private final static Pattern PATTERN_LOGIN_TICKET = Pattern.compile("^[0-9a-f]{12,}$");


    /**
     * aes解密对象缓存，每次new比较费时
     */
    private final static Map<String, AesCrypt> GROUP_ID_AES_CRYPT_CACHE = Collections.synchronizedMap(new LinkedHashMap(LOGIN_CONTEXT_CACHE_SIZE, 0.75F, true) {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > GROUP_ID_AES_CRYPT_SIZE;
        }
    });

    private final static Map<String, LoginContext> CIPHER_LOGIN_CONTEXT_CACHE = Collections.synchronizedMap(new LinkedHashMap(LOGIN_CONTEXT_CACHE_SIZE, 0.75F, true) {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > LOGIN_CONTEXT_CACHE_SIZE;
        }
    });

    /**
     * 当前登录上下文线程local
     */
    private final static ThreadLocal<LoginContext> loginContextThreadLocal = new ThreadLocal<LoginContext>();

    public static LoginContext get() {
        return loginContextThreadLocal.get();
    }

    public static void set(LoginContext loginContext) {
        loginContextThreadLocal.set(loginContext);
    }

    public static void remove() {
        loginContextThreadLocal.remove();
    }


    /**
     * 方便调用，其他信息使用get()方法拿
     *
     * @return
     */
    public static String getCurrentUsername() {
        LoginContext loginContext = LoginContext.get();
        return null == loginContext ? null : loginContext.getUsername();
    }

    public static Date getCurrentLoginTime() {
        LoginContext loginContext = LoginContext.get();
        return null == loginContext ? null : loginContext.getLoginTime();
    }

    public static String getCurrentLoginIp() {
        LoginContext loginContext = LoginContext.get();
        return null == loginContext ? null : loginContext.getLoginIp();
    }

    public static Date getCurrentCookieTime() {
        LoginContext loginContext = LoginContext.get();
        return null == loginContext ? null : loginContext.getCookieTime();
    }

    public static String getCurrentTicket() {
        LoginContext loginContext = LoginContext.get();
        return null == loginContext ? null : loginContext.getTicket();
    }

    public static boolean checkCipher(String cipher) throws Exception {
        return null == cipher ? false : PATTERN_LOGIN_TICKET.matcher(cipher).matches();
    }

    /**
     * 用cookie解析context
     * <p/>
     * 输入都是16进制字符串
     *
     * @param cipher
     * @param ssoConfigResponseDomain
     * @param rawTicket  true 代表cookieValue，包含cookie时间，登录时间，ip，用户名，和ticket（每次用登录时间，ip，用户名加密计算出来写入ticket属性）
     *                   false  代表ticket，这个是缓存中value，key，包含登录时间，ip，用户名，目前只有服务端验证用到
     *
     * @return
     * @throws Exception
     */
    public static LoginContext decryptContext(String cipher, SsoConfigResponseDomain ssoConfigResponseDomain, boolean rawTicket) throws Exception {
        try {
            if (!checkCipher(cipher)) {
                return null;
            }
            LoginContext context = CIPHER_LOGIN_CONTEXT_CACHE.get(cipher);
            if (null != context) {
                return context;
            }
            String plain = decryptFromHexLowercase(cipher, ssoConfigResponseDomain);
            int size = rawTicket ? 3 : 4;
            String[] arr = plain.split("\\s*,\\s*", size);
            if (null == arr || arr.length != size) {
                throw new RuntimeException("无法解析解密后的数据, " + plain);
            }
            // 构造context
            context = new LoginContext();
            int offset = size - 3; // 向后的偏移量
            Date loginDate = new Date(Long.valueOf(arr[0 + offset]) * 1000L);
            context.setLoginTime(loginDate);
            context.setLoginIp(arr[1 + offset]);
            context.setUsername(arr[2 + offset]); // username可能含有空格，放在最后
            if(!rawTicket){
                context.setCookieTime(DateUtils.addSeconds(loginDate, Integer.valueOf(arr[0])));
                context.setTicket(encryptToHexLowercase(plain.substring(plain.indexOf(",") + 1), ssoConfigResponseDomain));
            }
            CIPHER_LOGIN_CONTEXT_CACHE.put(cipher, context);
            return context;
        } catch (Exception e) {
            logger.error("解密ticket失败, cipher: " + cipher, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 加密返回cookieValue
     *
     * @param context
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public static String encryptContext(LoginContext context, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        try {
            // 时间从存秒数，压缩cookie大小
            StringBuffer sb = new StringBuffer("");
            sb.append((new Date().getTime() - context.getLoginTime().getTime()) / 1000L + ""); // cookie时间
            sb.append(",");
            sb.append(context.getLoginTime().getTime() / 1000L + "");
            sb.append(",");
            sb.append(context.getLoginIp());
            sb.append(",");
            sb.append(context.getUsername());
            // 加密后转成16进制小写
            return encryptToHexLowercase(sb.toString(), ssoConfigResponseDomain);
        } catch (Exception e) {
            logger.error("加密登录上下文失败, ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将明文转为byte数组
     * 用AES加密该byte数组，得到加密后的byte数组
     * 将加密后的byte数组转为16进制表示
     * @param plain
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public static String encryptToHexLowercase(String plain, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        return Hex.encodeHexString(getAesCrypt(ssoConfigResponseDomain).encryptByteArray(plain.getBytes(ssoConfigResponseDomain.getCharset())));
    }


    /**
     * 将16进制的密文转为byte数组
     * 用AES解密该byte数组为解密后的byte数组
     * 将解密后的byte数组还原为明文
     * @param cipher
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public static String decryptFromHexLowercase(String cipher, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        return new String(getAesCrypt(ssoConfigResponseDomain).decryptByteArray(Hex.decodeHex(cipher.toCharArray())), ssoConfigResponseDomain.getCharset());
    }


    public static String encrypt(String plain, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        try {
            return Base64.getUrlEncoder().encodeToString(getAesCrypt(ssoConfigResponseDomain).encryptByteArray(plain.getBytes()));
        } catch (Exception e) {
            logger.error("加密字符串失败, ", e);
            throw new RuntimeException(e);
        }
    }


    public static String decrypt(String cipher, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        try {
            return getAesCrypt(ssoConfigResponseDomain).decrypt(Base64.getUrlDecoder().decode(cipher));
        } catch (Exception e) {
            logger.error("解密字符串失败, ", e);
            throw new RuntimeException(e);
        }
    }


    private static AesCrypt getAesCrypt(SsoConfigResponseDomain ssoConfigResponseDomain) {
        AesCrypt aesCrypt = GROUP_ID_AES_CRYPT_CACHE.get(ssoConfigResponseDomain.getGroupId());
        if (null == aesCrypt) {
            aesCrypt = new AesCrypt(ssoConfigResponseDomain.getAesKey(), ssoConfigResponseDomain.getAesBit());
            GROUP_ID_AES_CRYPT_CACHE.put(ssoConfigResponseDomain.getGroupId(), aesCrypt);
        }
        return aesCrypt;
    }

    // =============================以下为cookie中的相关信息==============================

    private String username;

    private Date loginTime;

    private String loginIp;

    private Date cookieTime;

    private String ticket;

    public LoginContext() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getCookieTime() {
        return cookieTime;
    }

    public void setCookieTime(Date cookieTime) {
        this.cookieTime = cookieTime;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}