package com.zeroq6.sso.service;
/**
 * Created by icgeass on 2017/3/21.
 */

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


@Service
public class ConfigCheckService implements InitializingBean {

    @Value("${sso.primaryDomain}")
    private String ssoPrimaryDomain;

    @Value("${sso.domain}")
    private String ssoDomain;

    @Value("${sso.protocol}")
    private String ssoProtocol;

    @Value("${sso.port}")
    private String port;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!"https".equals(ssoProtocol) && !"http".equals(ssoProtocol)) {
            throw new RuntimeException("${profile.sso.protocol}只能配置http或https");
        }
        if (StringUtils.isBlank(ssoDomain) || StringUtils.isBlank(ssoPrimaryDomain)) {
            throw new RuntimeException("${profile.sso.domain}和${profile.sso.primaryDomain}配置不能为空");
        }
        if (!("." + ssoDomain).contains(ssoPrimaryDomain)) {
            throw new RuntimeException("${profile.sso.primaryDomain}不是${profile.sso.domain}的主域");
        }
        if (!Pattern.matches("\\d{1,5}", port)) {
            throw new RuntimeException("${profile.sso.port}只能为1-5位数字");
        }
        int p = Integer.valueOf(port);
        if (p < 1 || p > 65535) {
            throw new RuntimeException("${profile.sso.port}范围非法");
        }
    }
}
