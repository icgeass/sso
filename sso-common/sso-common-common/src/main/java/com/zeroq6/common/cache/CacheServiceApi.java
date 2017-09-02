
package com.zeroq6.common.cache;

public interface CacheServiceApi {


    int DEFAULT_EXPIRED_IN_SECONDS = 60 * 60 * 24 * 30;


    String get(String key) throws Exception;

    boolean set(String key, String value) throws Exception;

    boolean set(String key, String value, int expiredInSeconds) throws Exception;

    boolean remove(String key) throws Exception;


}
