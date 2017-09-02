package com.zeroq6.sso.web.client.config;

import com.zeroq6.sso.web.client.api.SsoServiceApi;
import com.zeroq6.sso.web.client.domain.SsoConfigRequestDomain;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;

/**
 * Created by icgeass on 2017/2/22.
 *
 * 该属性配置只允许初始化一次，并且必须成功
 * 初始化完成后不能修改
 *
 * 应用启动的时候初始化
 */
public class PropertyHolder {

    private static SsoConfigResponseDomain ssoConfigResponseDomain;

    private static SsoConfigRequestDomain ssoConfigRequestDomain;

    private static SsoServiceApi ssoServiceApi;

    private PropertyHolder(){}

    public static SsoConfigResponseDomain getSsoConfigResponseDomain() {
        return ssoConfigResponseDomain;
    }

    public static void setSsoConfigResponseDomain(SsoConfigResponseDomain ssoConfigResponseDomain) {
        PropertyHolder.ssoConfigResponseDomain = ssoConfigResponseDomain;
    }

    public static SsoConfigRequestDomain getSsoConfigRequestDomain() {
        return ssoConfigRequestDomain;
    }

    public static void setSsoConfigRequestDomain(SsoConfigRequestDomain ssoConfigRequestDomain) {
        PropertyHolder.ssoConfigRequestDomain = ssoConfigRequestDomain;
    }

    public static SsoServiceApi getSsoServiceApi() {
        return ssoServiceApi;
    }

    public static void setSsoServiceApi(SsoServiceApi ssoServiceApi) {
        PropertyHolder.ssoServiceApi = ssoServiceApi;
    }
}