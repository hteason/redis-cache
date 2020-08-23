package pers.htc.customredis.service;

import java.util.Map;

public interface IRedisService {

    /**
     * 设置键与值(添加)
     *
     * @param key
     *            键
     * @param value
     *            值
     */
    boolean set(String key, String value);

    /**
     * 设置键、值、生存时间(添加)
     *
     * @param key
     *            键
     * @param value
     *            值
     * @param seconds
     *            生存时间(按秒计算)
     */
    boolean setex(int dbIndex, String key, String value, int seconds);

    /**
     * 获取指定的key对应的值(查询)
     *
     * @param key
     *            键
     * @return 值
     */
    String get(String key);

    /**
     * 删除指定的键
     *
     * @param key
     *            键
     * @return 状态码
     */
    boolean del(String key);

    /**
     * 删除指定的键
     *
     * @param dbIndex
     * @param key
     * @return
     */
    boolean del(final int dbIndex, final String key);

    /**
     * 设置自增长的键
     *
     * @param key
     *            键
     * @return 自增长值
     */
    Long incr(String key);

    /**
     * hash操作
     *
     * @param cachename
     * @param key
     * @param value
     * @return
     */
    boolean hset(String cachename, String key, String value);

    /**
     * hash查询
     *
     * @param cachename
     * @param key
     * @return
     */
    String hget(String cachename, String key);

    /**
     * hash递增
     *
     * @param cachename
     * @param key
     * @return
     */
    boolean hincrByKey(final String cachename, String key);

    /**
     * hash递减
     *
     * @param cachename
     * @param key
     * @return
     */
    boolean hdimiByKey(final String cachename, String key);

    /**
     * set 添加操作
     *
     * @param key
     * @param value
     * @return
     */
    boolean sadd(String key, String value);

    /**
     *
     * @param key
     * @param value
     * @return
     */
    boolean srem(String key, String value);

    /**
     * set 的容量
     *
     * @param key
     * @return
     */
    Integer scard(String key);



    boolean flush(int dbIndex);


    /**
     * set 获取一个随机的元素
     *
     * @param key
     * @return
     */
    String spop(String key);

    Map<String, String> hgetAll(final String cachename);

    boolean hmset(int dbIndex,final String cachename, final Map map,int seconds);

    /**
     * 设置生存时间
     *
     * @param key
     *            键
     * @param seconds
     *            生存时间(按秒计算)
     * @return 状态码
     */
    boolean expire(String key, int seconds);

    boolean exists(String key);

    boolean hashKey(String cachename,String key);

    boolean setIfAbsent(final String key, final String value,final int second);

    boolean acquire(final String key,final String value,final long second);

    void release(final String key,final String value);
}
