package pers.htc.customredis.annotation;

import cn.hutool.core.annotation.Alias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisKeyParam {
    //默认为该参数的值，排除复杂实体对象
    String value();
}
