package pers.htc.customredis.aspect.model;

import lombok.Data;

/**
 * 存入数据库的数据格式
 * @param <T>
 */
@Data
public class CommonRedisDataModel<T> {
    /**
     * 缓存数据
     */
    private T data;
    /**
     * 请求失败次数
     */
    private Integer failCount;
    /**
     * 缓存过期的时间点,单位:毫秒
     */
    private Long expireTime;
}

