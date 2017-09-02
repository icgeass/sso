package com.zeroq6.sso.service;
/**
 * Created by icgeass on 2017/3/21.
 */

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ConfigCheckService implements InitializingBean {

    @Value("${sso.primaryDomain}")
    private String ssoPrimaryDomain;

    @Value("${sso.domain}")
    private String ssoDomain;

    @Value("${sso.protocol}")
    private String ssoProtocol;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!"https".equals(ssoProtocol) && !"http".equals(ssoProtocol)) {
            throw new RuntimeException("${profile.sso.protocol}只能配置http或https");
        }
        if (StringUtils.isBlank(ssoDomain) || StringUtils.isBlank(ssoPrimaryDomain)) {
            throw new RuntimeException("${profile.sso.domain}和${profile.sso.ssoPrimaryDomain}配置不能为空");
        }
        if (!("." + ssoDomain).contains(ssoPrimaryDomain)) {
            throw new RuntimeException("${profile.sso.ssoPrimaryDomain}不是${profile.sso.domain}的主域");
        }
    }
}