package com.zeroq6.sso.service.api.impl;
/**
 * Created by icgeass on 2017/2/23.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zeroq6.common.cache.CacheServiceApi;
import com.zeroq6.common.utils.MyTypeUtils;
import com.zeroq6.sso.service.api.SsoConfigServiceApi;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SsoConfigServiceImpl implements SsoConfigServiceApi, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private SsoConfigResponseDomain ssoConfigResponseDomain;

    @Value("${sso.service.group.config.maxGroupIdCount}")
    private int ssoServiceGroupConfigMaxGroupIdCount;

    @Value("${sso.service.group.config.cacheKeyPrefix}")
    private String ssoServiceGroupConfigCacheKeyPrefix;

    @Value("${sso.port}")
    private String port;

    @Value("${sso.protocol}")
    private String protocol;

    @Value("${sso.domain}")
    private String domain;

    // 一个groupId，对应的sso配置
    private final Map<String, SsoConfigResponseDomain> ssoConfigResponseDomainMap = new ConcurrentHashMap<String, SsoConfigResponseDomain>();


    @Autowired
    private CacheServiceApi cacheServiceApi;

    public SsoConfigServiceImpl() {

    }

    /**
     * 初始化获取groupId对应sso配置，其中的groupId需要单独设置
     *
     * 接入应用启动时首次调用get，设置对应属性后set，后续调用get则一定拿到set后的正确内容
     * 对于前端请求过来则会判断contains，也不会调用到get拿到错误内容
     *
     * @param groupId
     * @return
     */
    @Override
    public SsoConfigResponseDomain get(String groupId) {
        String cacheKey = getCacheKey(groupId);
        SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigResponseDomainMap.get(cacheKey);
        if (null != ssoConfigResponseDomain) {
            return ssoConfigResponseDomain;
        }
        try {
            String value = cacheServiceApi.get(cacheKey);
            if (StringUtils.isNotBlank(value)) {
                ssoConfigResponseDomain = JSON.parseObject(value, SsoConfigResponseDomain.class);
            } else {
                ssoConfigResponseDomain = copyDefaultSsoConfigResponse();
            }
            return ssoConfigResponseDomain;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void set(String groupId, SsoConfigResponseDomain ssoConfigResponseDomain) {
        groupId = SsoUtils.getGroupId(groupId);
        if (SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId)) {
            return;
        }
        if (ssoConfigResponseDomainMap.size() >= ssoServiceGroupConfigMaxGroupIdCount) {
            throw new RuntimeException("接入groupId超过" + ssoServiceGroupConfigMaxGroupIdCount + "限制");
        }
        String cacheKey = getCacheKey(groupId);
        try {
            cacheServiceApi.set(cacheKey, JSON.toJSONString(ssoConfigResponseDomain), Integer.MAX_VALUE);// 持久
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ssoConfigResponseDomainMap.put(cacheKey, ssoConfigResponseDomain);
    }

    @Override
    public void remove(String groupId, SsoConfigResponseDomain ssoConfigResponseDomain) {
        groupId = SsoUtils.getGroupId(groupId);
        if (SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId)) {
            return;
        }
        String cacheKey = getCacheKey(groupId);
        try {
            cacheServiceApi.remove(cacheKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ssoConfigResponseDomainMap.remove(cacheKey);
    }

    @Override
    public boolean contains(String groupId) {
        try {
            return SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId) || ssoConfigResponseDomainMap.containsKey(getCacheKey(groupId)) || StringUtils.isNotBlank(cacheServiceApi.get(getCacheKey(groupId)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (("https".equals(protocol) && !"443".equals(port)) || ("http".equals(protocol) && !"80".equals(port))) {
            ssoConfigResponseDomain.setLoginUrl(protocol + "://" + domain + ":" + port + "/sso/login");
            ssoConfigResponseDomain.setLogoutUrl(protocol + "://" + domain + ":" + port + "/sso/logout");
        }
        ssoConfigResponseDomainMap.put(getCacheKey(SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID), copyDefaultSsoConfigResponse());
    }

    private String getCacheKey(String groupId) {
        return ssoServiceGroupConfigCacheKeyPrefix + SsoUtils.getGroupId(groupId);
    }

    private SsoConfigResponseDomain copyDefaultSsoConfigResponse(){
        return MyTypeUtils.transfer(ssoConfigResponseDomain, new TypeReference<SsoConfigResponseDomain>() {});
    }
}

