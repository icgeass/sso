package com.zeroq6.sso.service.api.impl;

import com.zeroq6.sso.service.api.AuthenticationServiceApi;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by yuuki asuna on 2017/8/29.
 */

public class AuthenticationService implements AuthenticationServiceApi {

    private final static String PASSWORD_SALT = "login_salt_";

    private Map<String, String> accountInfoMap;

    public void setAccountInfoMap(Map<String, String> accountInfoMap) {
        this.accountInfoMap = accountInfoMap;
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }
        if (null == accountInfoMap || accountInfoMap.isEmpty()) {
            return false;
        }
        String pass = accountInfoMap.get(username);
        if (null == pass || !pass.equals(DigestUtils.md5Hex(PASSWORD_SALT + password))) {
            return false;
        }
        return true;
    }
}
