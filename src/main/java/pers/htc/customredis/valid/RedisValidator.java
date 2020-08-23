package pers.htc.customredis.valid;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.htc.customredis.model.CustomRedisModel;
import pers.htc.customredis.model.common.CommonRedisDataDTO;
import pers.htc.customredis.service.IRedisService;

import java.util.concurrent.TimeUnit;

@Component
public class RedisValidator {
    @Autowired
    private IRedisService redisService;

    public CommonRedisDataDTO valid(CustomRedisModel customRedisModel) {
        final String redisKey = customRedisModel.getRedisKey();

        //缓存是否有数据
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
    public <T> T handlerSuccessResponse(String redisKey, T data,CustomRedisModel customRedisModel) {

        //失效时间转为时间戳长度
        long expireTme = customRedisModel.getExpireTme();
        TimeUnit expireTimeUnit = customRedisModel.getExpireTimeUnit();

        //默认缓存失效时间：30分钟后，对应的毫秒数
        final long NORMAL_EXPIRE_TIMEMILLIS = expireTimeUnit.toMillis(expireTme);//30 * 60 * 1000;

        //只存储有数据的
        if (data != null) {
            //缓存
            CommonRedisDataDTO<T> cache = new CommonRedisDataDTO<>();
            cache.setData(data);
            cache.setFailCount(0);
            //过期时间为NORMAL_EXPIRE_TIMEMILLIS分钟后
            cache.setExpireTime(System.currentTimeMillis() + NORMAL_EXPIRE_TIMEMILLIS);
            redisService.set(redisKey, JSON.toJSONString(cache));
        }
        return data;
    }


    /**
     * EWMS请求失败处理方案：当失败次数达到MAX_FAIL_COUNT值时过期时间expireTime=60分钟，失败次数failCount=0
     *
     * @param redisKey           redis键
     * @param commonRedisDataDTO redis里缓存的数据
     */
    public void handlerFailResponse(String redisKey, CommonRedisDataDTO commonRedisDataDTO,CustomRedisModel customRedisModel) {
        //请求EWMS的最大失败次数
        final int MAX_FAIL_COUNT = customRedisModel.getFailCount();
        //达到MAX_FAIL_COUNT次请求失败后缓存的失效时间：60分钟后，对应的毫秒数
        final long FAIL_REQUEST_EXPIRE_TIMEMILLIS = 3600_000L;//60 * 60 * 1000;

        int failCount = commonRedisDataDTO.getFailCount();
        if (failCount + 1 == MAX_FAIL_COUNT) {
            //达到最大请求失败次数,将过期时间设置为60分钟后，失败次数=0
            commonRedisDataDTO.setExpireTime(System.currentTimeMillis() + FAIL_REQUEST_EXPIRE_TIMEMILLIS);
            commonRedisDataDTO.setFailCount(0);
        } else {
            //失败次数+1
            commonRedisDataDTO.setFailCount(failCount + 1);
        }
        //更新缓存次数、过期时间
        redisService.set(redisKey, JSON.toJSONString(commonRedisDataDTO));
    }
}
