package utils.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import utils.impl.ConnectionPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * FileName: RedsiClusterPool   获取的所有连接必须回收，要么关闭，要么回收到连接池
 * Author:   MAIBENBEN
 * Date:     2019/11/27 10:34
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class RedsiClusterPool implements ConnectionPool<JedisCluster> {

    private static final LinkedList<JedisCluster> pool = new LinkedList<JedisCluster>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RedsiClusterPool.class);
    private static Set<HostAndPort> HOST_AND_PORTS;
    private static int CONNECTION_TIEM_OUT;
    private static JedisPoolConfig JEDIS_POOL_CONFIG;
    private static int MAX_CONN = 30;
    private static int INIT_CONN = 20;
    private static int SO_TIME_OUT;
    private static int MAX_ATTEMPTS;
    private static Long MAX_WAIT = 2000L;
    private static int currentCount = 0;
    private static int tpmCount = 0;
    private static String auth;

    public RedsiClusterPool(Set<HostAndPort> nodes, Integer connectionTiemOut, JedisPoolConfig jedisPoolConfig) {
        HOST_AND_PORTS = nodes;
        CONNECTION_TIEM_OUT = connectionTiemOut;
        JEDIS_POOL_CONFIG = jedisPoolConfig;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    /**
     * 初始化线程池
     *
     * @param initConn    初始化的连接数个数
     * @param maxConn     设置的最大连接数，
     * @param maxWait     如果连接池内的连接使用完，
     * @param soTimeOut   我也不知道是啥
     * @param maxAttempts 我也不知道是啥
     */
    public void initPool(Integer initConn, Integer maxConn, Long maxWait, int soTimeOut, int maxAttempts) {
        INIT_CONN = initConn;
        MAX_CONN = maxConn;
        MAX_WAIT = maxWait;
        SO_TIME_OUT = soTimeOut;
        MAX_ATTEMPTS = maxAttempts;
        try {
            for (int i = 0; i < INIT_CONN; i++) {
                pool.add(getJedisCluster());
            }
            LOGGER.info("初始化 Redis 连接池成功：" + "当前初始化的连接数为: " + INIT_CONN + "最大连接数为：" + MAX_CONN + " 最大等待时间为：" + MAX_WAIT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("初始化 Redis 连接池失败: 主机地址为：" + HOST_AND_PORTS.toString() + "auth为： " + auth);
        }
    }

    private JedisCluster getJedisCluster() {
        JedisCluster jedisCluster;
        if (auth == null) {
            jedisCluster = new JedisCluster(HOST_AND_PORTS, CONNECTION_TIEM_OUT, SO_TIME_OUT, MAX_ATTEMPTS, JEDIS_POOL_CONFIG);
        } else {
            jedisCluster = new JedisCluster(HOST_AND_PORTS, CONNECTION_TIEM_OUT, SO_TIME_OUT, MAX_ATTEMPTS, auth, JEDIS_POOL_CONFIG);
        }
        return jedisCluster;
    }

    /**
     * @return 获取 Jedis 实例
     */
    public synchronized JedisCluster getResource() {
        JedisCluster jedisCluster = null;

        if (pool.size() < 1 && currentCount < MAX_CONN) {
            LOGGER.warn("初始化时的：" + MAX_CONN + "个连接已使用完" + " 将在追加初始化: " + (MAX_CONN - INIT_CONN) + " 个连接数");
            for (int i = currentCount; i < MAX_CONN; i++) {
                tpmCount++;
                pool.add(getJedisCluster());
            }
            currentCount++;
            LOGGER.info("当前以使用连接数为：" + currentCount + " 总的初始化连接数为： " + (INIT_CONN + tpmCount) + " 最大连接数为： " + MAX_CONN);
            jedisCluster = pool.remove();
        } else if (pool.size() < 1 && currentCount == MAX_CONN) {
            while (pool.size() < 1 && currentCount == MAX_CONN) {
                LOGGER.error("服务器繁忙请稍等");
                LOGGER.error("最大连接数为： " + MAX_CONN + " 已使用连接数为：" + currentCount + " 等待资源释放");
                try {
                    wait(MAX_WAIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            currentCount++;
            LOGGER.info("当前以使用连接数为：" + currentCount + " 初始化连接为： " + MAX_CONN + " 最大连接数为： " + MAX_CONN);
            jedisCluster = pool.remove();

        } else {
            currentCount++;
            LOGGER.info("当前以使用连接数为：" + currentCount + " 初始化连接为： " + (INIT_CONN + tpmCount) + " 最大连接数为： " + MAX_CONN);
            jedisCluster = pool.remove();
        }
        return jedisCluster;
    }

    /**
     * 将资源回收到资源池
     *
     * @param jedisCluster Jedis 实例
     * @return 回收是否成功
     */
    public synchronized void returnResource(JedisCluster jedisCluster) {

        currentCount--;
        pool.add(jedisCluster);
    }

    /**
     * 关闭连接，不建议使用此方法，因为关闭连接后还要再重新创建连接，
     * 建议将连接返回到redis连接池中
     *
     * @param jedisCluster Jedis 实例
     */
    public synchronized void close(JedisCluster jedisCluster) {
        if (null != jedisCluster)
            jedisCluster.close();
        currentCount--;
    }

    /**
     * 均衡连接数
     */
    public synchronized void reBalanced() {
        while (pool.size() != INIT_CONN) {
            if (tpmCount != 0 && pool.size() > INIT_CONN) {
                pool.remove().close();
                tpmCount--;
            } else
                pool.add(getJedisCluster());
        }
        LOGGER.info("当前以使用连接数为：" + currentCount + " 初始化连接为： " + (INIT_CONN + tpmCount) + " 最大连接数为： " + MAX_CONN);
    }

    public static void main(String[] args) {
        Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
        //一般选用slaveof从IP+端口进行增删改查，不用master
        nodes.add(new HostAndPort("172.10.10.120", 9000));
        nodes.add(new HostAndPort("172.10.10.121", 9000));
        nodes.add(new HostAndPort("172.10.10.122", 9000));
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
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
        final RedsiClusterPool redsiClusterPool = new RedsiClusterPool(nodes, 3000, jedisPoolConfig);
        redsiClusterPool.setAuth("4rGhQpgkPRzS");
        redsiClusterPool.initPool(5, 9, 2000L, 2000, 5);


        JedisCluster resource = redsiClusterPool.getResource();

        System.out.println(resource.hget("APPKEY_MAP", "29308d14718214d5b76654ac"));
        redsiClusterPool.returnResource(resource);


    }

}
