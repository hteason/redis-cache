package pers.htc.customredis.aspect;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.htc.customredis.model.CustomRedisModel;
import pers.htc.customredis.model.User;
import pers.htc.customredis.model.common.CommonRedisDataDTO;
import pers.htc.customredis.model.common.Result;
import pers.htc.customredis.valid.RedisValidator;

import java.lang.reflect.Type;

@Aspect
@Component
@Slf4j
public class RedisCacheAspect {

    @Autowired
    CustomRedisParser customRedisParser;
    @Autowired
    private RedisValidator redisValidator;

    @Pointcut("@annotation(pers.htc.customredis.annotation.CustomRedis)")
    private void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) {
        log.info("redis注解切面拦截开始");
        try {
            CustomRedisModel customRedisModel = customRedisParser.parse(pjp);
            String redisKey = customRedisModel.getRedisKey();

            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            Class clz = methodSignature.getReturnType();

            CommonRedisDataDTO redisDTO = redisValidator.valid(customRedisModel);
            if (redisDTO != null) {
                //判断是否过期
                boolean expired = redisDTO.isExpired();
                if (!expired) {
                    //未过期 直接返回
                    return transToClass(redisDTO.getData(), clz);
                }

                //已过期
                Object proceed = pjp.proceed();
                if (proceed == null) {
                    //失败处理
                    redisValidator.handlerFailResponse(redisKey, redisDTO,customRedisModel);
                    return transToClass(redisDTO.getData(), clz);
                } else {
                    //成功处理
                    return redisValidator.handlerSuccessResponse(redisKey, clz.cast(proceed),customRedisModel);
                }

            } else {
                //不存在
                Object proceed = pjp.proceed();
                if (proceed != null) {
                    //成功处理
                    return redisValidator.handlerSuccessResponse(redisKey, clz.cast(proceed),customRedisModel);
                }
                //失败则不处理
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private <T> T transToClass(Object obj, Class<T> clz) {
        String jsonStr = JSONObject.toJSONString(obj);
        return JSONObject.parseObject(jsonStr, clz);
    }
}
