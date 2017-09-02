package com.zeroq6.sso.web.client.domain;
/**
 * Created by icgeass on 2017/2/22.
 */

import java.io.Serializable;

public class SsoConfigRequestDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String clientVersion = "1.0.5";

    private String groupId;

    private int expiredInSeconds;

    private boolean singleSignExit;

    private String ignoreUriPrefix;

    private String ignoreUriSuffix;

    private String disallowUriPrefix;

    private String disallowUriSuffix;

    public String getClientVersion() {
        return clientVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getExpiredInSeconds() {
        return expiredInSeconds;
    }

    public void setExpiredInSeconds(int expiredInSeconds) {
        this.expiredInSeconds = expiredInSeconds;
    }

    public boolean isSingleSignExit() {
        return singleSignExit;
    }

    public void setSingleSignExit(boolean singleSignExit) {
        this.singleSignExit = singleSignExit;
    }

    public String getIgnoreUriPrefix() {
        return ignoreUriPrefix;
    }

    public void setIgnoreUriPrefix(String ignoreUriPrefix) {
        this.ignoreUriPrefix = ignoreUriPrefix;
    }

    public String getIgnoreUriSuffix() {
        return ignoreUriSuffix;
    }

    public void setIgnoreUriSuffix(String ignoreUriSuffix) {
        this.ignoreUriSuffix = ignoreUriSuffix;
    }

    public String getDisallowUriPrefix() {
        return disallowUriPrefix;
    }

    public void setDisallowUriPrefix(String disallowUriPrefix) {
        this.disallowUriPrefix = disallowUriPrefix;
    }

    public String getDisallowUriSuffix() {
        return disallowUriSuffix;
    }

    public void setDisallowUriSuffix(String disallowUriSuffix) {
        this.disallowUriSuffix = disallowUriSuffix;
    }
}