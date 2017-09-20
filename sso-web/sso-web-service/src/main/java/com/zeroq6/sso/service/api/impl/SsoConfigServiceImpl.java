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

    // 一个groupId，对应的sso配置
    private final Map<String, SsoConfigResponseDomain> ssoConfigResponseDomainMap = new ConcurrentHashMap<String, SsoConfigResponseDomain>();


    @Autowired
    private CacheServiceApi cacheServiceApi;

    public SsoConfigServiceImpl() {

    }

    @Override
    public SsoConfigResponseDomain get(String groupId) {
        groupId = SsoUtils.getGroupId(groupId);
        String cacheKey = getCacheKey(groupId);
        SsoConfigResponseDomain ssoConfigResponseDomain = ssoConfigResponseDomainMap.get(cacheKey);
        if(null != ssoConfigResponseDomain){
            return ssoConfigResponseDomain;
        }
        try {
            String value = cacheServiceApi.get(cacheKey);
            if(StringUtils.isNotBlank(value)){
                ssoConfigResponseDomain = JSON.parseObject(value, SsoConfigResponseDomain.class);
            }else{
                ssoConfigResponseDomain = ssoConfigResponseDomainMap.get(getCacheKey(SsoUtils.getGroupId(SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID)));
            }
            ssoConfigResponseDomainMap.put(cacheKey, ssoConfigResponseDomain); // 放入本地缓存
            return ssoConfigResponseDomain;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String groupId, SsoConfigResponseDomain ssoConfigResponseDomain) {
        groupId = SsoUtils.getGroupId(groupId);
        if(SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId)){
            return;
        }
        if (ssoConfigResponseDomainMap.size() >= ssoServiceGroupConfigMaxGroupIdCount) {
            // 不是严格限制，未同步配置无法纳入计数，get方法同步
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
        if(SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId)){
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
            return SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID.equals(groupId) || ssoConfigResponseDomainMap.containsKey(groupId) || StringUtils.isNotBlank(cacheServiceApi.get(getCacheKey(groupId)));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ssoConfigResponseDomainMap.put(getCacheKey(SsoUtils.getGroupId(SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID)), MyTypeUtils.transfer(ssoConfigResponseDomain, new TypeReference<SsoConfigResponseDomain>(){}));
    }

    private String getCacheKey(String groupId){
        return ssoServiceGroupConfigCacheKeyPrefix + SsoUtils.getGroupId(groupId);
    }
}

