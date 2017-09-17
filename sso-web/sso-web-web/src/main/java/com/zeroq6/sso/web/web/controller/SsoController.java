package com.zeroq6.sso.web.web.controller;
/**
 * Created by icgeass on 2017/2/22.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zeroq6.common.counter.Counter;
import com.zeroq6.common.counter.CounterService;
import com.zeroq6.common.security.RsaCrypt;
import com.zeroq6.sso.service.LoginCacheService;
import com.zeroq6.sso.service.LoginService;
import com.zeroq6.sso.service.api.SsoConfigServiceApi;
import com.zeroq6.sso.web.client.context.LoginContext;
import com.zeroq6.sso.web.client.domain.BaseResponseExtend;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.CookieUtils;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.*;

/**
 * todo 后续可以考虑加入登录密码+时间的RSA加密 ---不是sso内容，目前不加 --- 完成
 * todo cookie的readOnly， -- servlet 2.5不支持，3.1兼容性不好，目前不加 -- 完成
 * todo 登录用户，ip的错误次数限制，前台的优化等  -- 完成
 * todo 添加是否验证ip 开关 -- 一律加入
 * todo 字体 -- 不需要
 * todo 服务户端多机部署 -- 依赖数据库，实际应用时才加，不加 -- 使用缓存，完成
 */
@Controller
@RequestMapping("/sso")
public class SsoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginCacheService loginCacheService;

    @Autowired
    private SsoConfigServiceApi ssoConfigServiceImpl;

    @Autowired
    private CounterService counterService;

    @Autowired
    private RsaCrypt rsaCrypt;

    @Value("${counter.type.login.ip}")
    private String counterTypeLoginIp;

    @Value("${counter.type.login.username}")
    private String counterTypeLoginUsername;

    @Value("${sso.service.login.controller.prefix}")
    private String ssoServiceLoginControllerPrefix;

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(String username, String password, HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        String result = null;
        try {
            result = doLogin(username, password, null, request, response, view);
        } catch (Exception e) {
            logger.error("访问登录失败", e);
            result = toLoginPage(request, response, view, e.getMessage());
        }
        return result;
    }


    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        String result = null;
        try {
            result = doLogout(null, request, response, view);
        } catch (Exception e) {
            logger.error("登出失败", e);
            result = toLoginPage(request, response, view, e.getMessage());
        }
        return result;
    }


    @RequestMapping(value = "/{groupId}/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginWithGroupId(@PathVariable String groupId, String username, String password, HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        String result = null;
        try {
            result = doLogin(username, password, groupId, request, response, view);
        } catch (Exception e) {
            logger.error("访问登录失败", e);
            result = toLoginPage(request, response, view, e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/{groupId}/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logoutWithGroupId(@PathVariable String groupId, HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        String result = null;
        try {
            result = doLogout(groupId, request, response, view);
        } catch (Exception e) {
            logger.error("登出失败", e);
            result = toLoginPage(request, response, view, e.getMessage());
        }
        return result;
    }

    private String doLogin(String username, String password, String groupId, HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        view.addAttribute("publicKey", rsaCrypt.getPublicKeyBase64());
        // 拿配置
        SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigServiceImpl.get(groupId);
        // 如果ticket有效的，则说明跨域的登录验证或非法请求，重定向到来源页面并将ticket值加密重定向传给来源系统
        if (null != checkTicket(request, ssoConfigResponseDomain)) {
            String cookieValue = CookieUtils.get(request, ssoConfigResponseDomain.getCookieName());
            toFromPage(request, response, ssoConfigResponseDomain, cookieValue);
            return null; // 浏览器不会显示该页面，因为已经重定向了
        }
        String message = null;
        // GET访问登录页面
        if (SsoConfigResponseDomain.HTTP_METHOD_GET.equals(request.getMethod())) {
            // 删除验证失败的cookie，重新登录
            CookieUtils.delete(request, response, ssoConfigResponseDomain.getCookieName(), ssoConfigResponseDomain.getPrimaryDomain(), ssoServiceLoginControllerPrefix + ssoConfigResponseDomain.getGroupId());
            // POST提交登录
        } else if (SsoConfigResponseDomain.HTTP_METHOD_POST.equals(request.getMethod())) {
            // 登录错误次数验证
            Map<String, String> query = new HashMap<String, String>();
            query.put(counterTypeLoginIp, SsoUtils.getClientIp(request));
            query.put(counterTypeLoginUsername, username);
            List<Counter> counterList = counterService.get(query);
            for(Counter counter : counterList){
                if(counter.isLock()){
                    message = counter.getMessage();
                    CookieUtils.delete(request, response, ssoConfigResponseDomain.getCookieName(), ssoConfigResponseDomain.getPrimaryDomain(), ssoServiceLoginControllerPrefix + ssoConfigResponseDomain.getGroupId());
                    return toLoginPage(request, response, view, message);
                }
            }
            BaseResponseExtend<String> re = loginService.login(username, password, ssoConfigResponseDomain, request);
            if (re.isSuccess()) {
                // 更新计数器成功
                counterService.updateSuccess(counterList);
                // 加密
                String ticket = re.getBody(); // 登录成功返回ticket
                // 写cookie
                Cookie cookie = new Cookie(ssoConfigResponseDomain.getCookieName(), ticket);
                cookie.setDomain(ssoConfigResponseDomain.getPrimaryDomain());
                cookie.setPath("/"); // 指定path路径，如果写/，浏览器会将所有groupId的cookie带过来; @update 防止同域下的重定向,写/
                cookie.setMaxAge(ssoConfigResponseDomain.getExpiredInSeconds());
                CookieUtils.set(response, cookie);
                toFromPage(request, response, ssoConfigResponseDomain, ticket); // 判断如果是非单点登录域，则带ticket参数重定向
                logger.info("登录成功, " + JSON.toJSONString(counterList));
                return null;
            } else {
                if(!LoginService.LOGIN_CODE_FAILED_EXCEPTION.equals(re.getCode())){
                    counterService.updateFailed(counterList);
                    message = counterService.getMessage(counterList);
                }else{
                    message = re.getMessage();
                }
                logger.info("登录失败, " + JSON.toJSONString(counterList));
            }
        }
        return toLoginPage(request, response, view, message);
    }

    private String doLogout(String groupId, HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        view.addAttribute("publicKey", rsaCrypt.getPublicKeyBase64());
        SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigServiceImpl.get(groupId);
        LoginContext context = checkTicket(request, ssoConfigResponseDomain);
        if (null != context) {
            loginService.logout(context.getUsername(), ssoConfigResponseDomain);
        }
        CookieUtils.delete(request, response, ssoConfigResponseDomain.getCookieName(), ssoConfigResponseDomain.getPrimaryDomain(), "/");
        toFromPage(request, response, ssoConfigResponseDomain, null);
        return null;
    }


    /**
     * 检验cookie中是否含有登录信息，有并且有效则返回LoginContext，否则返回null
     *
     * @param request
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public LoginContext checkTicket(HttpServletRequest request, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        String cookieValue = CookieUtils.get(request, ssoConfigResponseDomain.getCookieName());
        // 本地验证cookieValue，合法性，ip，时间
        LoginContext context = LoginContext.decryptContext(cookieValue, ssoConfigResponseDomain, false);
        if (null == context) {
            return null;
        }
        String ip = SsoUtils.getClientIp(request);
        if ((null == ip) || (!"127.0.0.1".equals(ip) && !ip.equals(context.getLoginIp()))) {
            logger.error("非法请求, 登录ip: " + context.getLoginIp() + ", 来源ip: " + ip);
            return null;
        }
        Date expire = DateUtils.addSeconds(context.getLoginTime(), ssoConfigResponseDomain.getExpiredInSeconds());
        if (new Date().compareTo(expire) > 0) {
            logger.error("cookieValue已过期: cookieValue: " + cookieValue);
            return null;
        }
        // 是否远程验证
        if (ssoConfigResponseDomain.isSingleSignExit()) {
            String ticketCache = loginCacheService.getTicket(context.getUsername(), ssoConfigResponseDomain);
            if (null == ticketCache || !ticketCache.equals(context.getTicket())) {
                return null;
            }
        }
        return context;
    }

    /**
     * 重定向到参数ssoConfigResponseDomain.getReturnUrlName()指定的地址（如果指定带ticket重定向，则必须事先判断cookie中值有值并且合法）
     * <p/>
     * 如果连接不合法，则使用默认重定向地址
     *
     * @param request
     * @param response
     * @param ssoConfigResponseDomain
     * @param ticket
     * @throws Exception
     */
    private void toFromPage(HttpServletRequest request, HttpServletResponse response, SsoConfigResponseDomain ssoConfigResponseDomain, String ticket) throws Exception {
        String redirectUrl = null;
        try{
            redirectUrl = request.getQueryString(); // 这种方式不会解码编码过的ReturnUrl，getParameter会解码，并且出现乱码
            if (StringUtils.isNotBlank(redirectUrl)) {
                List<String> params = Arrays.asList(redirectUrl.split("&"));
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && kv[0].equals(ssoConfigResponseDomain.getReturnUrlName())) {
                        redirectUrl = LoginContext.decrypt(kv[1], ssoConfigResponseDomain);
                        redirectUrl = SsoUtils.unwrapReturnUrl(redirectUrl);
                        if(null == redirectUrl){
                            throw new RuntimeException("链接被篡改, " + SsoUtils.getClientIp(request));
                        }
                        redirectUrl = !redirectUrl.toLowerCase().startsWith("http") ? "http://" + redirectUrl : redirectUrl;
                        if (!UrlValidator.getInstance().isValid(redirectUrl)) {
                            redirectUrl = ssoConfigResponseDomain.getDefaultRedirectUrl();
                        } else {
                            if(null != ticket){
                                URL url = new URL(redirectUrl);
                                if (!("." + url.getHost()).contains(ssoConfigResponseDomain.getPrimaryDomain())) {
                                    String newTicket = String.valueOf(new Date().getTime() / 1000L) + "," + ticket;
                                    newTicket = LoginContext.encrypt(newTicket, ssoConfigResponseDomain); // 将ticket和当前时间加密重定向给单点应用
                                    loginCacheService.setRedirectTicket(newTicket, ssoConfigResponseDomain);
                                    redirectUrl = redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + ssoConfigResponseDomain.getTicketName() + "=" + newTicket;
                                } else {
                                    // 同域不需要重定向加密ticket
                                }
                            }
                        }
                        break;
                    } else {
                        redirectUrl = ssoConfigResponseDomain.getDefaultRedirectUrl();
                    }
                }
            } else {
                redirectUrl = ssoConfigResponseDomain.getDefaultRedirectUrl();
            }

        }catch (Exception e){
            redirectUrl = ssoConfigResponseDomain.getDefaultRedirectUrl();
            logger.error("获取重定向地址失败, URI: " + request.getRequestURI() + ", QUERY STRING: " + request.getQueryString(), e);
        }
        if (SsoConfigResponseDomain.AJAX_HEADER_VALUE.equalsIgnoreCase(request.getHeader(SsoConfigResponseDomain.AJAX_HEADER_NAME))) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", true);
            jsonObject.put("ReturnUrl", redirectUrl);
            SsoUtils.outString(response, jsonObject.toJSONString());
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    private String toLoginPage(HttpServletRequest request, HttpServletResponse response, Model view, String message) throws Exception {
        if (SsoConfigResponseDomain.AJAX_HEADER_VALUE.equalsIgnoreCase(request.getHeader(SsoConfigResponseDomain.AJAX_HEADER_NAME))) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", false);
            jsonObject.put("message", message);
            SsoUtils.outString(response, jsonObject.toJSONString());
            return null;
        }
        view.addAttribute("message", message);
        return "login";
    }


}