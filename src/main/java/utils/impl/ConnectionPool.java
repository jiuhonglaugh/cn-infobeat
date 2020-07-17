package utils.impl;

/**
 * FileName: ConnectionPool
 * Author:   MAIBENBEN
 * Date:     2019/11/27 10:30
 * History:
 * <author>          <time>          <version>          <desc>
 */
public interface ConnectionPool<E> {
    /**
     * 初始化连接池
     */
    void initPool(Integer initConn, Integer maxActive, Long maxWait, int soTimeOut, int maxAttempts);

    /**
     * 获取一个连接
     */

    E getResource();

    /**
     * 归还连接
     */
    void returnResource(E e);

    /**
     * 关闭连接
     *
     * @param e
     */

    void close(E e);
}
