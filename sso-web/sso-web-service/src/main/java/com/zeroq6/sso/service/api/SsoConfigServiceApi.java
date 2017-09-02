package com.zeroq6.sso.service.api;

import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;

/**
 * Created by icgeass on 2017/3/17.
 */
public interface SsoConfigServiceApi {

    SsoConfigResponseDomain get(String groupId);


    void set(String groupId, SsoConfigResponseDomain ssoConfigResponseDomain);


    void remove(String groupId, SsoConfigResponseDomain ssoConfigResponseDomain);
}
