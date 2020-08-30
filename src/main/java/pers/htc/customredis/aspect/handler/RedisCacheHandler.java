package pers.htc.customredis.aspect.handler;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.htc.customredis.aspect.dto.CommonRedisDataDTO;
import pers.htc.customredis.aspect.model.CommonRedisDataModel;
import pers.htc.customredis.aspect.model.CustomRedisModel;
import pers.htc.customredis.service.IRedisService;

import java.util.concurrent.TimeUnit;

/**
 * 接口响应数据成功/失败缓存处理
 *
 * @author huangtingcheng
 * @since 2020-08-24 22:30
 **/
@Component
public class RedisCacheHandler {
    @Autowired
    private IRedisService redisService;

    /**
     * 根据解析后的key值获取redis数据
     *
     * @param redisKey redis的key值
     * @return 缓存数据对应实体对象
     */
    public CommonRedisDataDTO getCacheData(final String redisKey) {

        //redis缓存是否有数据
        if (redisService.exists(redisKey)) {
            String dataStr = redisService.get(redisKey);
            CommonRedisDataDTO cacheDTO = JSONArray.parseObject(dataStr, CommonRedisDataDTO.class);

            long expireTime = cacheDTO.getExpireTime();
            //缓存是否已过期
            if (System.currentTimeMillis() < expireTime) {
                //未过期，直接返回数据
                cacheDTO.setExpired(false);
                return cacheDTO;
            }
            //已过期,返回旧数据
            cacheDTO.setExpired(true);
            return cacheDTO;
        }
        return null;
    }

    /**
     * 成功请求到权限json串，解析数据并存入redis，转换为权限对象列表并返回
     * failCount = 0,expireTime = 30min后
     *
     * @param redisKey 键
     * @param data     ewms系统响应
     * @return 权限对象列表
     */
    public <T> T handleSuccessResponse(String redisKey, T data, CustomRedisModel customRedisModel) {

        //失效时间转为时间戳长度
        long expireTime = customRedisModel.getExpireTme();
        TimeUnit expireTimeUnit = customRedisModel.getExpireTimeUnit();
        if (expireTime < 0) {
            //设置时间不允许负数,若为负数,设置默认为1个时间单位
            expireTime = 1L;
        }

        //默认缓存失效时间：对应{@link CustomRedis#expireTme()}
        final long NORMAL_EXPIRE_TIMEMILLIS = expireTimeUnit.toMillis(expireTime);

        //只存储有数据的
        if (data != null) {
            //缓存
            CommonRedisDataModel<T> cache = new CommonRedisDataModel<>();
            cache.setData(data);
            cache.setFailCount(0);
            //过期时间为NORMAL_EXPIRE_TIMEMILLIS分钟后
            cache.setExpireTime(System.currentTimeMillis() + NORMAL_EXPIRE_TIMEMILLIS);
            redisService.set(redisKey, JSON.toJSONString(cache));
        }
        return data;
    }


    /**
     * EWMS请求失败处理方案：当失败次数达到MAX_FAIL_COUNT值时过期时间延长到extendTime个时间单位后，失败次数failCount=0
     *
     * @param redisKey           redis键
     * @param commonRedisDataDTO redis里缓存的数据
     * @param customRedisModel   @CustomRedis注解里的值
     */
    public void handleFailResponse(String redisKey, CommonRedisDataDTO commonRedisDataDTO, CustomRedisModel customRedisModel) {
        CommonRedisDataModel redisDataModel = JSON.parseObject(JSONObject.toJSONString(commonRedisDataDTO), CommonRedisDataModel.class);
        long extendTime = customRedisModel.getExtendTime();
        if (extendTime < 0) {
            extendTime = 1L;
        }
        TimeUnit expireTimeUnit = customRedisModel.getExpireTimeUnit();

        //请求EWMS的最大失败次数
        final int MAX_FAIL_COUNT = customRedisModel.getFailCount();
        //达到MAX_FAIL_COUNT次请求失败后缓存的失效时间：60分钟后，对应的毫秒数
        final long FAIL_REQUEST_EXPIRE_TIMEMILLIS = expireTimeUnit.toMillis(extendTime);

        int failCount = redisDataModel.getFailCount();
        if (failCount + 1 == MAX_FAIL_COUNT) {
            //达到最大请求失败次数,将过期时间设置为60分钟后，失败次数=0
            redisDataModel.setExpireTime(System.currentTimeMillis() + FAIL_REQUEST_EXPIRE_TIMEMILLIS);
            redisDataModel.setFailCount(0);
        } else {
            //失败次数+1
            redisDataModel.setFailCount(failCount + 1);
        }
        //更新缓存次数、过期时间
        redisService.set(redisKey, JSON.toJSONString(redisDataModel));
    }
}

