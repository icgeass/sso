package com.zeroq6.sso.web.client.domain;

import java.io.Serializable;

/**
 * Created by icgeass on 2017/2/21.
 *
 * 接入单点登录的客户端系统启动时从单点登录的服务系统拿到相关配置
 *
 * 暂时没有使用公私钥验证的场景，公钥不加
 */
public class SsoConfigResponseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String DEFAULT_SSO_GROUP_ID = "";

    /**
     * 处理ajax请求被拦截到登录页面的问题
     */
    public final static String AJAX_HEADER_NAME = "X-Requested-With";

    public final static String AJAX_HEADER_VALUE = "XMLHttpRequest";

    public final static String HEADER_LOCATION_NAME = "Location";

    public final static String HTTP_METHOD_GET = "GET";

    public final static String HTTP_METHOD_POST = "POST";

    public final static int HTTP_PORT_80 = 80;

    public final static int HTTP_PORT_443 = 443;

    // 服务端单点登录系统的版本号
    private String serverVersion;

    // 登录url
    private String loginUrl;

    // 退出url
    private String logoutUrl;

    // cooke名
    private String cookieName;

    // 接入客户端groupId
    private String groupId = DEFAULT_SSO_GROUP_ID;

    // cookie过期时间
    private int expiredInSeconds;

    // AES加密cookie使用的key
    private String aesKey;

    // AES加密cookie使用的位数，支持128,192,256
    private int aesBit;

    // 默认拦截uri的前缀，英文逗号分隔
    private String disallowUriPrefix;

    // 默认拦截uri的前缀，英文逗号分隔
    private String disallowUriSuffix;

    // 默认不拦截uri的前缀，英文逗号分隔
    private String ignoreUriPrefix;

    // 默认不拦截的uri后缀，英文逗号分隔
    private String ignoreUriSuffix;

    // ticket名，目前只有非单点登录系统域使用
    private String ticketName;

    // 跳到登录页面，传递当前请求链接的参数名
    private String returnUrlName;

    // url，字符串加解密使用的编码方式
    private String charset;

    // 是否单点退出
    private boolean singleSignExit;

    // 默认跳转链接
    private String defaultRedirectUrl;

    // 登录服务端应用主域
    private String primaryDomain;

    // 跨域登录时从重定向参数中获取ticket的时间限制
    private int redirectTicketExpireInSeconds;

    public SsoConfigResponseDomain() {
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getExpiredInSeconds() {
        return expiredInSeconds;
    }

    public void setExpiredInSeconds(int expiredInSeconds) {
        this.expiredInSeconds = expiredInSeconds;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public int getAesBit() {
        return aesBit;
    }

    public void setAesBit(int aesBit) {
        this.aesBit = aesBit;
    }

    public String getIgnoreUriPrefix() {
        return ignoreUriPrefix;
    }

    public void setIgnoreUriPrefix(String ignoreUriPrefix) {
        this.ignoreUriPrefix = ignoreUriPrefix;
    }

    public String getIgnoreUriSuffix() {
        return ignoreUriSuffix;
    }

    public void setIgnoreUriSuffix(String ignoreUriSuffix) {
        this.ignoreUriSuffix = ignoreUriSuffix;
    }

    public String getDisallowUriPrefix() {
        return disallowUriPrefix;
    }

    public void setDisallowUriPrefix(String disallowUriPrefix) {
        this.disallowUriPrefix = disallowUriPrefix;
    }

    public String getDisallowUriSuffix() {
        return disallowUriSuffix;
    }

    public void setDisallowUriSuffix(String disallowUriSuffix) {
        this.disallowUriSuffix = disallowUriSuffix;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getReturnUrlName() {
        return returnUrlName;
    }

    public void setReturnUrlName(String returnUrlName) {
        this.returnUrlName = returnUrlName;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isSingleSignExit() {
        return singleSignExit;
    }

    public void setSingleSignExit(boolean singleSignExit) {
        this.singleSignExit = singleSignExit;
    }

    public String getDefaultRedirectUrl() {
        return defaultRedirectUrl;
    }

    public void setDefaultRedirectUrl(String defaultRedirectUrl) {
        this.defaultRedirectUrl = defaultRedirectUrl;
    }

    public String getPrimaryDomain() {
        return primaryDomain;
    }

    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = primaryDomain;
    }

    public int getRedirectTicketExpireInSeconds() {
        return redirectTicketExpireInSeconds;
    }

    public void setRedirectTicketExpireInSeconds(int redirectTicketExpireInSeconds) {
        this.redirectTicketExpireInSeconds = redirectTicketExpireInSeconds;
    }
}