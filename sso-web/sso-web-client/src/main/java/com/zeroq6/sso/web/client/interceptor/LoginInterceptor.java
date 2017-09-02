package com.zeroq6.sso.web.client.interceptor;
/**
 * Created by icgeass on 2017/2/22.
 */

import com.zeroq6.sso.web.client.api.SsoServiceApi;
import com.zeroq6.sso.web.client.config.PropertyHolder;
import com.zeroq6.sso.web.client.context.LoginContext;
import com.zeroq6.sso.web.client.domain.BaseResponse;
import com.zeroq6.sso.web.client.domain.SsoConfigRequestDomain;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.CookieUtils;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 单点登录客户端拦截逻辑
 * <p/>
 * 接口可以分开实现
 */
public class LoginInterceptor implements HandlerInterceptor, InitializingBean {


    private final List<String> ignoreUriPrefixList = new ArrayList<String>();

    private final List<String> ignoreUriSuffixList = new ArrayList<String>();

    private final List<String> disallowUriPrefixList = new ArrayList<String>();

    private final List<String> disallowUriSuffixList = new ArrayList<String>();



    private final static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    private SsoServiceApi ssoServiceApi;

    private String groupId = SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID;

    private boolean singleSignExit = true;

    private int expireInSeconds;

    private String ignoreUriPrefix;

    private String ignoreUriSuffix;

    private String disallowUriPrefix;

    private String disallowUriSuffix;

    public void setSsoServiceApi(SsoServiceApi ssoServiceApi) {
        this.ssoServiceApi = ssoServiceApi;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setSingleSignExit(boolean singleSignExit) {
        this.singleSignExit = singleSignExit;
    }

    public void setExpireInSeconds(int expireInSeconds) {
        this.expireInSeconds = expireInSeconds;
    }

    public void setIgnoreUriPrefix(String ignoreUriPrefix) {
        this.ignoreUriPrefix = ignoreUriPrefix;
    }

    public void setIgnoreUriSuffix(String ignoreUriSuffix) {
        this.ignoreUriSuffix = ignoreUriSuffix;
    }

    public void setDisallowUriPrefix(String disallowUriPrefix) {
        this.disallowUriPrefix = disallowUriPrefix;
    }

    public void setDisallowUriSuffix(String disallowUriSuffix) {
        this.disallowUriSuffix = disallowUriSuffix;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // uri
        lab1 :for (String ignore : ignoreUriPrefixList) {
            if (uri.startsWith(ignore)) {
                for (String disallow : disallowUriPrefixList) {
                    if (uri.startsWith(disallow)) {
                        break lab1;
                    }
                }
                return true;
            }
        }
        lab2: for (String ignore : ignoreUriSuffixList) {
            if (uri.endsWith(ignore)) {
                for (String disallow : disallowUriSuffixList) {
                    if (uri.endsWith(disallow)) {
                        break lab2;
                    }
                }
                return true;
            }
        }
        // 拿sso配置
        SsoConfigResponseDomain ssoConfigResponseDomain = PropertyHolder.getSsoConfigResponseDomain();
        // 取cookieValue
        Cookie cookie = CookieUtils.getCookie(request, ssoConfigResponseDomain.getCookieName());
        String cookieValue = null == cookie ? null : cookie.getValue();
        String redirectTicket = request.getParameter(ssoConfigResponseDomain.getTicketName());
        if (StringUtils.isBlank(cookieValue)) {
            cookieValue = redirectTicket;
            if (StringUtils.isBlank(cookieValue)) {
                logout(request, response, false, false);
                return false;
            }
            // 如果是重定向的ticket，这个ticket是<时间秒数,原cookieValue>
            cookieValue = LoginContext.decrypt(cookieValue, ssoConfigResponseDomain);
            String[] array = cookieValue.split(",", 2); // 实际在也只可能有两个
            if (new Date().compareTo(DateUtils.addSeconds(new Date(Long.valueOf(array[0]) * 1000), ssoConfigResponseDomain.getRedirectTicketExpireInSeconds())) > 0) {
                logger.error("重定向ticket已过期, 将跳转到服务器重新生成: " + cookieValue);
                logout(request, response, false, false);
                return false; // 如果过期则重定向到单点登录服务器生成最新的
            }
            cookieValue = array[1]; // 真实cookieValue
        }
        // 本地验证cookieValue，合法性，ip，时间
        LoginContext context = LoginContext.decryptContext(cookieValue, PropertyHolder.getSsoConfigResponseDomain(), false);
        if (null == context) {
            logout(request, response, false, false);
            return false;
        }
        String ip = SsoUtils.getClientIp(request);
        if ((null == ip) || (!"127.0.0.1".equals(ip) && !ip.equals(context.getLoginIp()))) {
            logger.error("非法请求, 登录ip: " + context.getLoginIp() + ", 来源ip: " + ip);
            logout(request, response, false, false);
            return false;
        }
        // 防止cookieValue被人为添加到请求头，从而cookieValue永久有效
        // 验证过期时间为cookie更新时间 + xxx 秒，避免因为时间差导致sso客户端验证过期，服务端验证没有过期引起的循环重定向
        Date expire = DateUtils.addSeconds(context.getCookieTime(), ssoConfigResponseDomain.getExpiredInSeconds() + 1200);
        if (new Date().compareTo(expire) > 0) {
            logger.error("cookieValue已过期: cookieValue: " + cookieValue);
            logout(request, response, false, false);
            return false;
        }
        // cookie为null，但是拿到了context，说明是跨域登录，应用单独写cookie
        if (null == cookie) {
            // 验证重定向ticket是否合法
            BaseResponse<String> result = ssoServiceApi.validateRedirectTicket(redirectTicket, PropertyHolder.getSsoConfigRequestDomain());
            if (!result.isSuccess()) {
                logger.error("验证重定向ticket失败, 将跳转到服务器重新生成, redirectTicket" + redirectTicket + ", info: " + cookieValue);
                logout(request, response, false, false);
                return false;
            }
            // 写cookie
            cookie = new Cookie(ssoConfigResponseDomain.getCookieName(), cookieValue);
            cookie.setMaxAge(ssoConfigResponseDomain.getExpiredInSeconds());
            cookie.setPath("/"); // 跨域一律写根路径
            // 不写主域也没有任何问题，其他子域会到单点登录服务器登录，由于会带着单点登录域的cookieValue，所以会验证通过，返回合法cookieValue，然后应用自己再写cookie（当前域，/）
            // 对于退出：
            // 服务端维护groupId（没有则为""）和对应sso配置，每次请求服务端验证，可实现单点退出
            // 服务端cookie名用${groupId}.${默认cookie名}，写主域和/
            cookie.setDomain(SsoUtils.getCookieDomain(request));
            CookieUtils.set(response, cookie);
        } else {
            // 验证ticket是否合法
            if (ssoConfigResponseDomain.isSingleSignExit()) {
                BaseResponse<String> res = ssoServiceApi.validateTicket(context.getTicket(), PropertyHolder.getSsoConfigRequestDomain());
                if (!res.isSuccess()) {
                    logout(request, response, false, false);
                    return false;
                }
            }
            //
            // 间隔一定时间更新cookie值，但是ticket不能变
            if (new Date().compareTo(DateUtils.addSeconds(context.getCookieTime(), ssoConfigResponseDomain.getExpiredInSeconds() / 2)) > 0) {
                cookie = new Cookie(ssoConfigResponseDomain.getCookieName(), LoginContext.encryptContext(context, ssoConfigResponseDomain));
                cookie.setDomain(SsoUtils.getCookieDomain(request));
                cookie.setPath("/");
                cookie.setMaxAge(ssoConfigResponseDomain.getExpiredInSeconds());
                CookieUtils.set(response, cookie);
            }
        }
        // 验证通过，如果带有ssoConfigResponseDomain.getTicketName()参数则重定向去掉
        // 情况1，服务端重定向；情况2，多开窗口重复登录
        if(SsoConfigResponseDomain.HTTP_METHOD_GET.equals(request.getMethod()) && StringUtils.isNotBlank(request.getParameter(ssoConfigResponseDomain.getTicketName()))){
            String queryString = SsoUtils.getQueryString(request, ssoConfigResponseDomain.getTicketName());
            String redirectUrl = request.getRequestURL() + (null != queryString ? queryString : "");
            doRedirect(request, response, redirectUrl);
            return false;
        }
        // 线程绑定登录信息
        LoginContext.set(context);
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束显示移除登录信息
        LoginContext.remove();
    }

    ////////////////////////////////////////////

    public void afterPropertiesSet() throws Exception {
        // sso 客户端配置
        SsoConfigRequestDomain ssoConfigRequestDomain = new SsoConfigRequestDomain();
        ssoConfigRequestDomain.setGroupId(groupId); // 服务端处理groupId，原样传值
        ssoConfigRequestDomain.setSingleSignExit(singleSignExit);
        ssoConfigRequestDomain.setExpiredInSeconds(expireInSeconds);
        ssoConfigRequestDomain.setIgnoreUriPrefix(ignoreUriPrefix);
        ssoConfigRequestDomain.setIgnoreUriSuffix(ignoreUriSuffix);
        ssoConfigRequestDomain.setDisallowUriPrefix(disallowUriPrefix);
        ssoConfigRequestDomain.setDisallowUriSuffix(disallowUriSuffix);
        // 单点登录配置
        BaseResponse<SsoConfigResponseDomain> response = ssoServiceApi.getSsoConfigDomain(ssoConfigRequestDomain);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }
        SsoConfigResponseDomain ssoConfigResponseDomain = response.getBody();
        // 存储配置到静态变量
        PropertyHolder.setSsoConfigRequestDomain(ssoConfigRequestDomain);
        PropertyHolder.setSsoConfigResponseDomain(ssoConfigResponseDomain);
        PropertyHolder.setSsoServiceApi(ssoServiceApi);
        // disallow的uri
        String disallowPrefix = ssoConfigResponseDomain.getDisallowUriPrefix();
        if(StringUtils.isNotBlank(disallowPrefix)){
            disallowUriPrefixList.addAll(Arrays.asList(disallowPrefix.split("\\s*,\\s*")));
        }
        String disallowSuffix = ssoConfigResponseDomain.getDisallowUriSuffix();
        if(StringUtils.isNotBlank(disallowSuffix)){
            disallowUriSuffixList.addAll(Arrays.asList(disallowSuffix.split("\\s*,\\s*")));
        }
        // 不拦截的uri配置
        // http://stackoverflow.com/questions/7488643/how-to-convert-comma-separated-string-to-arraylist
        String ignorePrefix = ssoConfigResponseDomain.getIgnoreUriPrefix();
        if (StringUtils.isNotBlank(ignorePrefix)) {
            ignoreUriPrefixList.addAll(Arrays.asList(ignorePrefix.split("\\s*,\\s*")));
        }
        String ignoreSuffix = ssoConfigResponseDomain.getIgnoreUriSuffix();
        if (StringUtils.isNotBlank(ignoreSuffix)) {
            ignoreUriSuffixList.addAll(Arrays.asList(ignoreSuffix.split("\\s*,\\s*")));
        }
        logger.info("单点登录拦截器初始化完成！");
    }


    /**
     * 客户端logout
     * <p/>
     * 会将response重定向，
     * 调用后立即将当前请求return掉
     *
     * @param request
     * @param response
     * @throws Exception
     */
    private static void logout(HttpServletRequest request, HttpServletResponse response, boolean toLogOutPage, boolean returnHomePage) throws Exception {
        SsoConfigResponseDomain ssoConfigResponseDomain = PropertyHolder.getSsoConfigResponseDomain();
        // 前缀
        String redirectUrl = toLogOutPage ? ssoConfigResponseDomain.getLogoutUrl() : ssoConfigResponseDomain.getLoginUrl();
        redirectUrl = redirectUrl + "?" + ssoConfigResponseDomain.getReturnUrlName() + "=";
        String returnUrl = null;
        // 拼接返回链接
        if (returnHomePage) {
            returnUrl = "https".equals(request.getScheme()) ? "https://" : "http://";
            returnUrl += request.getServerName();
            int port = request.getServerPort();
            if (SsoConfigResponseDomain.HTTP_PORT_80 != port && SsoConfigResponseDomain.HTTP_PORT_443 != port) {
                returnUrl = returnUrl + ":" + port;
            }
            returnUrl += "/";
            returnUrl = SsoUtils.wrapReturnUrl(returnUrl); // 防止非法篡改
            redirectUrl += LoginContext.encrypt(returnUrl, ssoConfigResponseDomain);
        } else {
            String queryString = SsoUtils.getQueryString(request, ssoConfigResponseDomain.getTicketName());
            returnUrl = request.getRequestURL() + (null != queryString ? queryString : "");
            returnUrl = SsoUtils.wrapReturnUrl(returnUrl);
            redirectUrl += LoginContext.encrypt(returnUrl, ssoConfigResponseDomain);
        }
        // 删除cookie
        CookieUtils.delete(request, response, ssoConfigResponseDomain.getCookieName(), SsoUtils.getCookieDomain(request), "/");
        doRedirect(request, response, redirectUrl);
    }

    private static void doRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException {
        if (SsoConfigResponseDomain.AJAX_HEADER_VALUE.equalsIgnoreCase(request.getHeader(SsoConfigResponseDomain.AJAX_HEADER_NAME))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader(SsoConfigResponseDomain.HEADER_LOCATION_NAME, redirectUrl);
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logout(request, response, true, true);
    }

}