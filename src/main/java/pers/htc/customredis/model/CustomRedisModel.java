package pers.htc.customredis.model;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class CustomRedisModel {

    //键
    String key;

    String redisKey;

    //允许请求失败次数，超过后将
    int failCount;

    //多久后过期，-1代表永久保存
    long expireTme;

    //多少个时间单位后过期
    TimeUnit expireTimeUnit;

    long extendTime;

    TimeUnit extendTimeUnit;
}
