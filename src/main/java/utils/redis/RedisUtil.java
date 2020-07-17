package utils.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * FileName: RedisUtil
 * Author:   MAIBENBEN
 * Date:     2020/5/9 12:03
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class RedisUtil {
    private String hosts;
    private String host;
    private int port;
    private String auth;
    private Set<HostAndPort> nodes;
    public boolean isCluster = false;


    public RedisUtil(String hosts, String auth) {
        this.hosts = hosts;
        this.auth = auth;
        initHost();
    }

    private void initHost() {
        String[] hostAndPorts = hosts.split(",");
        if (hostAndPorts.length > 1) {
            isCluster = true;
            nodes = new LinkedHashSet<HostAndPort>();
            for (String hostAndPort : hostAndPorts) {
                String[] split = hostAndPort.split(":");
                nodes.add(new HostAndPort(split[0], Integer.valueOf(split[1])));
            }
        } else {
            for (String hostAndPort: hostAndPorts){
                String[] split = hostAndPort.split(":");
                host = split[0];
                port = Integer.valueOf(split[1]);
            }
        }
    }

    public Object getjedis() {
        if (isCluster) {
            RedisCluster redisCluster = new RedisCluster(nodes, auth);
            return redisCluster.getJedis();
        } else {
            RedisPool redisPool = new RedisPool(host, port, auth);
            return redisPool.getJedis();
        }
    }
}
