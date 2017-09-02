package com.zeroq6.sso.service;
/**
 * Created by icgeass on 2017/2/23.
 */

import com.zeroq6.common.cache.CacheServiceApi;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 登录缓存，groupId+username 为key，value为加密后的ticket
 */
@Service
public class LoginCacheService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CacheServiceApi cacheServiceApi;


    @Value("${sso.service.login.cacheKeyPrefix}")
    private String ssoServiceLoginCacheKeyPrefix;


    /**
     * 用groupId和用户名获得用户登录ticket
     *
     * @param username
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public String getTicket(String username, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        String value = cacheServiceApi.get(getLoginCacheKey(username, ssoConfigResponseDomain.getGroupId()));
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }


    /**
     * 设置登录ticket
     *
     * @param username
     * @param ssoConfigResponseDomain
     * @param ticket
     * @throws Exception
     */
    public boolean setTicket(String username, SsoConfigResponseDomain ssoConfigResponseDomain, String ticket) throws Exception {
        int expire = ssoConfigResponseDomain.getExpiredInSeconds();
        return cacheServiceApi.set(getLoginCacheKey(username, ssoConfigResponseDomain.getGroupId()), ticket, expire);

    }


    /**
     * @param username
     * @param ssoConfigResponseDomain
     * @throws Exception
     */
    public boolean removeTicket(String username, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        return cacheServiceApi.remove(getLoginCacheKey(username, ssoConfigResponseDomain.getGroupId()));
    }


    /***
     * 设置重定向ticket
     *
     * @param key
     * @param ssoConfigResponseDomain
     * @throws Exception
     */
    public boolean setRedirectTicket(String key, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        return cacheServiceApi.set(getRedirectCacheKey(key, ssoConfigResponseDomain.getGroupId()), key, ssoConfigResponseDomain.getRedirectTicketExpireInSeconds());
    }


    /**
     * @param key
     * @param ssoConfigResponseDomain
     * @throws Exception
     */
    public String getRedirectTicket(String key, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        String value = cacheServiceApi.get(getRedirectCacheKey(key, ssoConfigResponseDomain.getGroupId()));
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }


    /**
     * @param key
     * @param ssoConfigResponseDomain
     * @return
     * @throws Exception
     */
    public boolean removeRedirectTicket(String key, SsoConfigResponseDomain ssoConfigResponseDomain) throws Exception {
        return cacheServiceApi.remove(getRedirectCacheKey(key, ssoConfigResponseDomain.getGroupId()));
    }

    /**
     * 获取登录缓存key
     *
     * @param username
     * @param groupId
     * @return
     */
    private String getLoginCacheKey(String username, String groupId) {
        return ssoServiceLoginCacheKeyPrefix + SsoUtils.getGroupId(groupId) + "_" + username;
    }

    private String getRedirectCacheKey(String key, String groupId) {
        return ssoServiceLoginCacheKeyPrefix  + "REDIRECT_" + SsoUtils.getGroupId(groupId) + "_" + key;
    }


}