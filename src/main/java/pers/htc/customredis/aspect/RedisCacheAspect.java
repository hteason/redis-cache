package pers.htc.customredis.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.htc.customredis.aspect.dto.CommonRedisDataDTO;
import pers.htc.customredis.aspect.handler.ClassTypeHandler;
import pers.htc.customredis.aspect.handler.RedisCacheHandler;
import pers.htc.customredis.aspect.model.CustomRedisModel;
import pers.htc.customredis.aspect.parser.CustomRedisAnnotationParser;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangtingcheng
 * @since 2020-08-24
 **/
@Aspect
@Component
@Slf4j
public class RedisCacheAspect {

    @Autowired
    CustomRedisAnnotationParser customRedisParser;
    @Autowired
    private RedisCacheHandler redisCacheHandler;

    @Pointcut("@annotation(pers.htc.customredis.annotation.CustomRedis)")
    private void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        //处理返回类型
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        ClassTypeHandler classTypeHandler = new ClassTypeHandler(methodSignature.getMethod());
        Class<?> clz = classTypeHandler.getOuterClass();
        if (clz == void.class) {
            log.error("返回值为void,缓存注解失效");
            return pjp.proceed();
//            throw new RuntimeException("返回值为void,缓存注解失效");
        }

        CustomRedisModel customRedisModel = customRedisParser.parse(pjp);
        String redisKey = customRedisModel.getRedisKey();
        CommonRedisDataDTO currentRedisDTO = redisCacheHandler.getCacheData(redisKey);
        try {
            if (currentRedisDTO != null) {
                //redis有数据
                boolean expired = currentRedisDTO.isExpired();
                //判断是否过期
                if (!expired) {
                    //未过期 直接返回
                    return classTypeHandler.transToClassTypeObject(currentRedisDTO.getData());
                }

                //已过期,重新请求接口数据
                Object newData = pjp.proceed();
                if (isNullOrEmpty(newData)) {
                    //失败处理
                    redisCacheHandler.handleFailResponse(redisKey, currentRedisDTO, customRedisModel);
                    return classTypeHandler.transToClassTypeObject(currentRedisDTO.getData());
                } else {
                    //成功处理
                    return redisCacheHandler.handleSuccessResponse(redisKey, classTypeHandler.transToClassTypeObject(newData), customRedisModel);
                }
            } else {
                //redis无对应数据,请求接口
                Object proceed = pjp.proceed();
                if (!isNullOrEmpty(proceed)) {
                    //成功处理
                    return redisCacheHandler.handleSuccessResponse(redisKey, classTypeHandler.transToClassTypeObject(proceed), customRedisModel);
                }
                //失败则不处理
            }
        } catch (Throwable throwable) {
            //接口出异常，如果存在缓存,则直接返回原缓存数据,否则返回null
            if (Objects.nonNull(currentRedisDTO)) {
                redisCacheHandler.handleFailResponse(redisKey, currentRedisDTO, customRedisModel);
                return currentRedisDTO.getData();
            }
            throwable.printStackTrace();
        }
        return null;
    }


    /**
     * 检查数据是否为空
     * 集合:null || size是否为0
     * 对象数组:null || 长度是否为0
     * 基本数据类型数组:
     * 对象:null
     */
    private boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return ((Object[]) obj).length == 0;
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        return false;
    }
}

