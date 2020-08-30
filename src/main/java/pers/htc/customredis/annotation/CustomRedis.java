package pers.htc.customredis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomRedis {
    //键
    String key();

    //允许请求失败次数，超过后将自动延长时间，默认10次
    int failCount() default 10;

    //多久后过期，-1代表永久保存
    long expireTme() default -1L;

    //多少个时间单位后过期
    TimeUnit expireTimeUnit() default TimeUnit.MINUTES;

    //过期后远端接口挂掉时，为缓存加时,默认0不延长,暂定0会删除redis数据
    long extendTime() default 0L;

    //延长时间单位
    TimeUnit extendTimeUnit() default TimeUnit.MINUTES;
}
