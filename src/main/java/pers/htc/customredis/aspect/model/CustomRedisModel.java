package pers.htc.customredis.aspect.model;


import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * 从注解获取的数据封装类
 *
 * @author huangtingcheng
 * @since 2020-08-24
 **/
@Data
public class CustomRedisModel {

    /**
     * 键,支持SpEL表达式.
     * 已对SpEL进行自定义模板处理,变量用{}包裹
     */
    String key;

    /**
     * 通过SpEL解析后真正存到redis的key
     */
    String redisKey;

    /**
     * 允许请求失败次数，超过后将延长过期时间到 {@link CustomRedisModel#extendTime}个{#link CustomRedisModel#extendTimeUnit}时间单位后
     */
    int failCount;

    /**
     * 多久后过期，-1代表永久保存
     */
    long expireTme;

    /**
     * 多少个时间单位后过期
     */
    TimeUnit expireTimeUnit;

    /**
     * 达到最大失败次数后缓存数据延长的时间
     */
    long extendTime;

    /**
     * 延长时间单位
     */
    TimeUnit extendTimeUnit;
}
