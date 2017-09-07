package com.zeroq6.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 连接池和连接池配置整个应用只有一份，每个线程只持有一个资源，线程结束时自动关闭
 * <p>
 * Created by yuuki asuna on 2016/10/20.
 */
public class RedisCacheService implements CacheServiceApi, InitializingBean {


    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 写入成功redis返回
     */
    private final static String OK = "OK" ;

    private static ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<Jedis>();

    private static JedisPoolConfig jedisPoolConfig;

    private static JedisPool jedisPool;


    private Integer expiredInSeconds = DEFAULT_EXPIRED_IN_SECONDS;

    private String host;

    private Integer port;

    private Integer minIdle;
    private Integer maxIdle;

    private Integer maxTotal;

    private Integer maxWaitMillis;


    public RedisCacheService() {

    }

    public void setExpiredInSeconds(int expiredInSeconds) {
        this.expiredInSeconds = expiredInSeconds;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    /**
     * 获得一个连接实例
     *
     * @return
     */
    public Jedis getResource() {
        if (null == jedisThreadLocal.get()) {
            final Jedis jedis = jedisPool.getResource();
            final Thread targetThread = Thread.currentThread();
            jedisThreadLocal.set(jedis);
            // 这种方式如果获取资源的链接不关闭，比如说线程池，那边始终无法归还资源
            new Thread() {
                @Override
                public void run() {
                    try {
                        logger.info("监听归还redis链接, " + targetThread.getName());
                        targetThread.join();
                    } catch (Exception e) {
                        logger.error("加入线程阻塞失败", e);
                    } finally {
                        try {
                            returnResource(jedis);
                            logger.info("归还redis链接成功, " + targetThread.getName());
                        } catch (Exception e) {
                            logger.error("归还redis连接失败", e);
                        }
                    }
                }
            }.start();
        }
        return jedisThreadLocal.get();
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
        return getResource().get(key);
    }

    @Override
    public boolean set(String key, String value) throws Exception {
        return set(key, value, expiredInSeconds);
    }

    @Override
    public boolean set(String key, String value, int expiredInSeconds) throws Exception {
        return OK.equals(getResource().setex(key, expiredInSeconds, value));
    }


    @Override
    public boolean remove(String key) {
        return getResource().del(key) > 0;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        jedisPoolConfig = new JedisPoolConfig();
        // 连接池配置
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        // 如果线程池满阻塞则等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 测试实例是否可用
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnCreate(false);
        jedisPoolConfig.setTestOnReturn(false);
        // 扫描空闲实例是否有效，如果验证失败则销毁，setTimeBetweenEvictionRunsMillis必须大于0才有效
        jedisPoolConfig.setTestWhileIdle(true);
        // 扫描空闲实例是否有效的时间间隔，毫秒
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(120 * 1000);
        // 每次扫描空闲对象的次数
        jedisPoolConfig.setNumTestsPerEvictionRun(10);
        // 最小空闲对象保留时间
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60 * 1000);
        jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }
}
