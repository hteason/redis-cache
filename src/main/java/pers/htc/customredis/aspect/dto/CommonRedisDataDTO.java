package pers.htc.customredis.aspect.dto;

import lombok.Data;

/**
 * 在切面操作的数据类
 *
 * @author huangtingcheng
 * @since 2020-08-24
 **/
@Data
public class CommonRedisDataDTO<T> {
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
    /**
     * 是否已过期
     */
    private boolean expired;
}

