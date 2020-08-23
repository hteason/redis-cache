package pers.htc.customredis.model.common;

import lombok.Data;

@Data
public class CommonRedisDataDTO<T> {
    //权限树数据
    private T data;
    //请求失败次数
    private Integer failCount;
    //缓存过期的时间点
    private Long expireTime;
    //是否已过期
    private boolean isExpired;
}
