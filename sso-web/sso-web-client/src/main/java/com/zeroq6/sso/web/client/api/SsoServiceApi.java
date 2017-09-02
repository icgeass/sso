package com.zeroq6.sso.web.client.api;

import com.zeroq6.sso.web.client.domain.BaseResponse;
import com.zeroq6.sso.web.client.domain.SsoConfigRequestDomain;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;

/**
 * Created by icgeass on 2017/2/22.
 */
public interface SsoServiceApi {

    /**
     * 配置单点退出时，每次请求验证ticket
     * @param ticket
     * @param ssoConfigRequestDomain
     * @return
     */
    BaseResponse<String> validateTicket(String ticket, SsoConfigRequestDomain ssoConfigRequestDomain);

    /**
     * 验证重定向ticket
     * @param ticket
     * @return
     */
    BaseResponse<String> validateRedirectTicket(String ticket, SsoConfigRequestDomain ssoConfigRequestDomain);



    /**
     * 容器启动时从单点登录服务器拿到单点登录配置
     * @return
     */
    BaseResponse<SsoConfigResponseDomain> getSsoConfigDomain(SsoConfigRequestDomain ssoConfigRequestDomain);

}
