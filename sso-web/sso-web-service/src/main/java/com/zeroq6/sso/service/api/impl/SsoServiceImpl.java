package com.zeroq6.sso.service.api.impl;
/**
 * Created by icgeass on 2017/2/23.
 */

import com.alibaba.fastjson.JSON;
import com.zeroq6.sso.service.LoginCacheService;
import com.zeroq6.sso.service.api.SsoConfigServiceApi;
import com.zeroq6.sso.web.client.api.SsoServiceApi;
import com.zeroq6.sso.web.client.context.LoginContext;
import com.zeroq6.sso.web.client.domain.BaseResponse;
import com.zeroq6.sso.web.client.domain.SsoConfigRequestDomain;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class SsoServiceImpl implements SsoServiceApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoginCacheService loginCacheService;

    @Autowired
    private SsoConfigServiceApi ssoConfigServiceImpl;

    @Value("${sso.service.login.controller.prefix}")
    private String ssoServiceLoginControllerPrefix;

    @Value("${sso.service.cookie.maxExpiredInSeconds}")
    private int cookieMaxExpiredInSeconds;

    @Value("${sso.service.cookie.minExpiredInSeconds}")
    private int cookieMinExpiredInSeconds;


    /**
     * 如果配置了单点退出，依据缓存验证登录是否有效，有效则返回key对应存储的UserInfo
     *
     * @param ticket
     * @param ssoConfigRequestDomain
     * @return
     */
    @Override
    public BaseResponse<String> validateTicket(String ticket, SsoConfigRequestDomain ssoConfigRequestDomain) {
        try {
            SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigServiceImpl.get(ssoConfigRequestDomain.getGroupId());
            LoginContext context = transferLoginContext(ticket, ssoConfigRequestDomain);
            String ticketCache = loginCacheService.getTicket(context.getUsername(), ssoConfigResponseDomain);
            if (null == ticketCache || !ticketCache.equals(ticket)) {
                return new BaseResponse<String>(false, "ticket已失效", null);
            }
            return new BaseResponse<String>(true, "成功", null);
        } catch (Exception e) {
            logger.error("验证ticket异常, ticket: " + ticket, e);
            return new BaseResponse<String>(false, e.getMessage(), null);
        }
    }

    @Override
    public BaseResponse<String> validateRedirectTicket(String ticket, SsoConfigRequestDomain ssoConfigRequestDomain) {
        try {
            if (null == ssoConfigRequestDomain) {
                throw new RuntimeException("ssoConfigRequestDomain不能为空");
            }
            SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigServiceImpl.get(ssoConfigRequestDomain.getGroupId());
            boolean bl = loginCacheService.removeRedirectTicket(ticket, ssoConfigResponseDomain);
            if (!bl) {
                return new BaseResponse<String>(false, "验证失败", null);
            }
            return new BaseResponse<String>(true, "成功", null);
        } catch (Exception e) {
            logger.error("验证ticket异常, ticket: " + ticket, e);
            return new BaseResponse<String>(false, e.getMessage(), null);
        }
    }


    /**
     * 转换ticket，ssoConfigRequestDomain为context
     * @param ticket
     * @param ssoConfigRequestDomain
     * @return
     * @throws Exception
     */
    private LoginContext transferLoginContext(String ticket, SsoConfigRequestDomain ssoConfigRequestDomain) throws Exception {
        if (null == ssoConfigRequestDomain) {
            throw new RuntimeException("ssoConfigRequestDomain不能为空");
        }
        SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigServiceImpl.get(ssoConfigRequestDomain.getGroupId());
        LoginContext context = LoginContext.decryptContext(ticket, ssoConfigResponseDomain, true);
        if (null == context) {
            throw new RuntimeException("ticket不合法");
        }
        return context;
    }


    /**
     * 客户端应用启动时拿到对应单点登录配置
     *
     * @param ssoConfigRequestDomain
     * @return
     */
    @Override
    public BaseResponse<SsoConfigResponseDomain> getSsoConfigDomain(SsoConfigRequestDomain ssoConfigRequestDomain) {
        try {
            if (null == ssoConfigRequestDomain) {
                throw new RuntimeException("ssoConfigRequestDomain不能为空");
            }
            String groupId = SsoUtils.getGroupId(ssoConfigRequestDomain.getGroupId());
            SsoConfigResponseDomain re = ssoConfigServiceImpl.get(groupId);
            if (!re.getServerVersion().equals(ssoConfigRequestDomain.getClientVersion())) {
                throw new RuntimeException("版本号: " + ssoConfigRequestDomain.getClientVersion() + "与服务端不匹配, 请升级至" + re.getServerVersion());
            }
            // 如果配置和合法的groupId，设置个性化配置
            // ！！！！默认的groupId对应的SsoConfigResponseDomain，不能修改，否则会更改默认的配置
            if (!SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId)) {
                re.setGroupId(groupId);
                // 如果传入groupId的处理
                re.setSingleSignExit(ssoConfigRequestDomain.isSingleSignExit());
                int expire = ssoConfigRequestDomain.getExpiredInSeconds();
                if (expire >= cookieMinExpiredInSeconds && expire <= cookieMaxExpiredInSeconds) {
                    re.setExpiredInSeconds(expire);
                }
                // 这几个属性只能设置一次, 否则重叠
                if(!re.getCookieName().contains(ssoConfigRequestDomain.getGroupId() + ".")){
                    re.setLoginUrl(re.getLoginUrl().replaceFirst(ssoServiceLoginControllerPrefix, ssoServiceLoginControllerPrefix + ssoConfigRequestDomain.getGroupId() + "/"));
                    re.setLogoutUrl(re.getLogoutUrl().replaceFirst(ssoServiceLoginControllerPrefix, ssoServiceLoginControllerPrefix + ssoConfigRequestDomain.getGroupId() + "/"));
                    re.setCookieName(ssoConfigRequestDomain.getGroupId() + "." + re.getCookieName());
                }
            }
            // 拦截路径是否设置groupId均可配置，默认groupId过期时间不能在客户端配置，否则如果是单点登录域的主域，可能影响其他接入应用
            String disallowUriPrefix = ssoConfigRequestDomain.getDisallowUriPrefix();
            if (StringUtils.isNotBlank(disallowUriPrefix)) {
                re.setDisallowUriPrefix(disallowUriPrefix);
            }
            String disallowUriSuffix = ssoConfigRequestDomain.getDisallowUriSuffix();
            if (StringUtils.isNotBlank(disallowUriSuffix)) {
                re.setDisallowUriSuffix(disallowUriSuffix);
            }
            String ignoreUriPrefix = ssoConfigRequestDomain.getIgnoreUriPrefix();
            if (StringUtils.isNotBlank(ignoreUriPrefix)) {
                re.setIgnoreUriPrefix(ignoreUriPrefix);
            }
            String ignoreUriSuffix = ssoConfigRequestDomain.getIgnoreUriSuffix();
            if (StringUtils.isNotBlank(ignoreUriSuffix)) {
                re.setIgnoreUriSuffix(ignoreUriSuffix);
            }
            // 将groupId对应的配置存入缓存，groupId为SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID，则在初始化时放入
            ssoConfigServiceImpl.set(groupId, re);
            return new BaseResponse<SsoConfigResponseDomain>(true, "成功", re);
        } catch (Exception e) {
            logger.error("获取服务端配置异常, ssoConfigRequestDomain: " + JSON.toJSONString(ssoConfigRequestDomain), e);
            return new BaseResponse<SsoConfigResponseDomain>(false, e.getMessage(), null);
        }
    }


}
