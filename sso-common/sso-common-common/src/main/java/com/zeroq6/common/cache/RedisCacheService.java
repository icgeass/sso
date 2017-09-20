package com.zeroq6.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 */
public class RedisCacheService implements CacheServiceApi {


    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 写入成功redis返回
     */
    private final String OK = "OK" ;

    private JedisPool jedisPool;

    private Integer expiredInSeconds = DEFAULT_EXPIRED_IN_SECONDS;


    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void setExpiredInSeconds(Integer expiredInSeconds) {
        this.expiredInSeconds = expiredInSeconds;
    }

    public RedisCacheService() {

    }


    /**
     * 获得一个连接实例
     *
     * @return
     */
    public Jedis getResource() {
        return jedisPool.getResource();
    }

    /**
     * 归还连接实例
     */
    private void returnResource(Jedis jedis) {
        // 方法jedisPool.returnResource(jedis); 已被弃用
        if (null != jedis) {
            jedis.close();
        }
    }

    /**
     * 当系统关闭时调用，可在spring中配置
     */
    public void destroy() {
        jedisPool.destroy();
    }


    @Override
    public String get(String key) {
        Jedis jedis = null;
        try{
            jedis = getResource();
            return jedis.get(key);
        }catch (Exception e){
            logger.error("获取缓存值失败, key, " + key);
            throw new RuntimeException(e);
        }finally {
            returnResource(jedis);
        }
    }

    @Override
    public boolean set(String key, String value) throws Exception {
        return set(key, value, expiredInSeconds);
    }

    @Override
    public boolean set(String key, String value, int expiredInSeconds) throws Exception {
        Jedis jedis = null;
        try{
            jedis = getResource();
            return OK.equals(jedis.setex(key, expiredInSeconds, value));
        }catch (Exception e){
            logger.error("设置缓存值失败, key: " + key + ", value: " + value);
            throw new RuntimeException(e);
        }finally {
            returnResource(jedis);
        }
    }


    @Override
    public boolean remove(String key) {
        Jedis jedis = null;
        try{
            jedis = getResource();
            return getResource().del(key) > 0;
        }catch (Exception e){
            logger.error("移除缓存值失败, key: " + key);
            throw new RuntimeException(e);
        }finally {
            returnResource(jedis);
        }
    }

}
