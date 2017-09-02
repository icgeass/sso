package com.zeroq6.sso.service.api;

/**
 * Created by yuuki asuna on 2017/8/29.
 */
public interface AuthenticationServiceApi {

    boolean authenticate(String username, String password);
}
