package com.zeroq6.sso.service;
/**
 * Created by icgeass on 2017/2/23.
 */

import com.zeroq6.common.cache.CacheServiceApi;
import com.zeroq6.common.security.RsaCrypt;
import com.zeroq6.sso.service.api.AuthenticationServiceApi;
import com.zeroq6.sso.web.client.context.LoginContext;
import com.zeroq6.sso.web.client.domain.BaseResponse;
import com.zeroq6.sso.web.client.domain.BaseResponseExtend;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@Service
public class LoginService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final static String LOGIN_CODE_SUCCESS = "0";
    public final static String LOGIN_CODE_FAILED_USER_NAME_PASSWORD_ERROR = "1";
    public final static String LOGIN_CODE_FAILED_ILLEGAL_TIME = "2";
    public final static String LOGIN_CODE_FAILED_ILLEGAL_EXPIRED = "3";
    public final static String LOGIN_CODE_FAILED_EXCEPTION = "100";

    @Autowired
    private LoginCacheService loginCacheService;

    @Autowired
    private RsaCrypt rsaCrypt;

    @Autowired
    private CacheServiceApi cacheServiceApi;

    @Autowired
    private AuthenticationServiceApi authenticationServiceApi;


    @Value("${sso.service.login.expireInSeconds}")
    private int ssoServiceLoginExpireInSeconds;

    public BaseResponseExtend<String> login(String username, String password, SsoConfigResponseDomain ssoConfigResponseDomain, HttpServletRequest request) throws Exception {
        try {
            String decrypt = rsaCrypt.decryptFromBase64String(password);
            String [] arr = decrypt.split("\\s*,\\s*", 2);
            Date serverTime = new Date();
            Date clientTime = new Date(Long.valueOf(arr[0]));
            // 1. 客户端产生的加密串expireSeconds内有效
            if(!(serverTime.compareTo(DateUtils.addSeconds(clientTime, - ssoServiceLoginExpireInSeconds)) > 0 && serverTime.compareTo(DateUtils.addSeconds(clientTime, ssoServiceLoginExpireInSeconds)) < 0)){
                logger.error("非法请求" + LOGIN_CODE_FAILED_ILLEGAL_TIME + ", " + SsoUtils.getClientIp(request));
                return new BaseResponseExtend<String>(false, LOGIN_CODE_FAILED_ILLEGAL_TIME, "非法请求", null);
            }
            // 2. 一次验证
            // 先1后2，过期时间要比expireSeconds大，否则在一个expireSeconds周期内，可能同一password请求两次，1通过，2也通过
            if(StringUtils.isNotBlank(cacheServiceApi.get(password))){
                logger.error("非法请求" + LOGIN_CODE_FAILED_ILLEGAL_EXPIRED + ", " + SsoUtils.getClientIp(request));
                return new BaseResponseExtend<String>(false, LOGIN_CODE_FAILED_ILLEGAL_EXPIRED, "非法请求", null);
            }
            boolean bl = cacheServiceApi.set(password, password, ssoServiceLoginExpireInSeconds * 3); //
            if(!bl){
                throw new RuntimeException("cache error 1");
            }
            if (authenticationServiceApi.authenticate(username, arr[1])) {
                String ip = SsoUtils.getClientIp(request);
                LoginContext context = new LoginContext();
                context.setLoginIp(ip);
                context.setUsername(username);
                context.setLoginTime(serverTime);
                String cookieValue = LoginContext.encryptContext(context,ssoConfigResponseDomain);
                context = LoginContext.decryptContext(cookieValue, ssoConfigResponseDomain, false);
                bl = loginCacheService.setTicket(username, ssoConfigResponseDomain, context.getTicket());
                if(!bl){
                    throw new RuntimeException("cache error 2");
                }
                return new BaseResponseExtend<String>(true, LOGIN_CODE_SUCCESS, "成功", cookieValue);
            }
            return new BaseResponseExtend<String>(false, LOGIN_CODE_FAILED_USER_NAME_PASSWORD_ERROR, "用户名或密码错误", null);
        } catch (Exception e) {
            logger.error("登录失败, ", e);
            return new BaseResponseExtend<String>(false, LOGIN_CODE_FAILED_EXCEPTION,  e.getMessage(), null);
        }

    }

    public BaseResponse<String> logout(String username, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        try {
            if(ssoConfigResponseDomain.isSingleSignExit()){
                loginCacheService.removeTicket(username, ssoConfigResponseDomain);
            }
            return new BaseResponse<String>(true, "成功", null);
        } catch (Exception e) {
            return new BaseResponse<String>(false, e.getMessage(), null);
        }
    }

}