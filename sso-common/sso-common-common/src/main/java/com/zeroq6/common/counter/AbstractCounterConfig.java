package com.zeroq6.common.counter;

/**
 * Created by icgeass on 2017/4/19.
 */
public abstract class AbstractCounterConfig {

    // 优先级（数字越小，获取message时候排序越前）
    private int priority = 0;
    // 最大重试次数
    private int maxTimes = 5;
    // 锁定时长
    private int lockSeconds = 86400; // 60*60*24，1天
    // 缓存时长
    private int cacheSeconds = 2592000; // 60*60*24*30，30天
    // 缓存前缀
    private String cacheKeyPrefix = "COUNTER_";
    // 时间格式
    private String datePatternString = "yyyy-MM-dd HH:mm:ss";

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public void setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
    }

    public int getLockSeconds() {
        return lockSeconds;
    }

    public void setLockSeconds(int lockSeconds) {
        this.lockSeconds = lockSeconds;
    }

    public int getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(int cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public String getCacheKeyPrefix() {
        return cacheKeyPrefix;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    public String getDatePatternString() {
        return datePatternString;
    }

    public void setDatePatternString(String datePatternString) {
        this.datePatternString = datePatternString;
    }
}
