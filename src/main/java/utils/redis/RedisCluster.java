package utils.redis;

import redis.clients.jedis.*;

import java.util.*;

/**
 * FileName: RedisCluster
 * Author:   MAIBENBEN
 * Date:     2019/11/26 23:16
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class RedisCluster {

    private static JedisCluster jedis;
    private static JedisPoolConfig jedisPoolConfig;
    private static JedisPool jedisPool;

    public RedisCluster(Set<HostAndPort> nodes, String auth) {

        if (null == jedisPoolConfig)
            initJedisConfig(new Properties());

        //设置auth Password
        if (null == jedis)
            jedis = new JedisCluster(nodes, 5000, 3000, 5, auth, jedisPoolConfig);

    }

    public RedisCluster(Set<HostAndPort> nodes) {
        if (null == jedisPoolConfig)
            initJedisConfig(new Properties());

        //未设置auth Password
        if (null == jedis)
            jedis = new JedisCluster(nodes, 5000, 3000, 200, jedisPoolConfig);

    }

    /**
     * 初始化  jedis config
     *
     * @param pro 参数
     */
    private void initJedisConfig(Properties pro) {
        jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(100);
        // 最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(500);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(0);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
        jedisPoolConfig.setMaxWaitMillis(2000); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(true);
    }

    public JedisCluster getJedis() {
        return jedis;
    }

    public void close(JedisCluster jedis) {
        if (null != jedis)
            jedis.close();
    }


    public static void main(String[] args) {
        Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
        //一般选用slaveof从IP+端口进行增删改查，不用master
        nodes.add(new HostAndPort("172.10.10.120", 9000));
        nodes.add(new HostAndPort("172.10.10.121", 9000));
        nodes.add(new HostAndPort("172.10.10.122", 9000));
        RedisCluster redisCluster = new RedisCluster(nodes, "AUTH_PWD");
        JedisCluster jedis = redisCluster.getJedis();

        System.out.println(RedisCluster.jedis.get("mykey"));
        redisCluster.close(jedis);

    }
}
