package utils.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * FileName: Redis
 * Author:   MAIBENBEN
 * Date:     2020/5/9 11:47
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class RedisPool {
    private JedisPool jedisPool;
    private String host;
    private int port;
    private String auth;

    public RedisPool(String host, int port, String auth) {
        this.host = host;
        this.port = port;
        this.auth = auth;
    }

    public Jedis getJedis() {
        if (jedisPool == null) {
            synchronized (RedisPool.class) {
                if (jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxIdle(10);
                    poolConfig.setMaxTotal(10);
                    poolConfig.setMaxWaitMillis(1000);
                    jedisPool = new JedisPool(poolConfig, host, port);

                }
            }
        }
        Jedis resource = jedisPool.getResource();
        resource.auth(auth);
        return resource;
    }

    public static void close(Jedis jedis) {
        jedis.close();

    }

    public static void main(String[] args) {

        RedisPool redis = new RedisPool("172.10.22.101", 7006, "3edcvfr4");
        Jedis jedis = redis.getJedis();
        System.out.println(jedis.hgetAll("pa"));
        redis.close(jedis);
    }

}
