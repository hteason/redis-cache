package pers.htc.customredis.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import pers.htc.customredis.service.IRedisService;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xiongcongcong
 * @Date: 2018-09-07 15:01
 */
@Service
public class RedisServiceImpl implements IRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    /**
     * lua脚本
     */
    private static final String COMPARE_AND_DELETE =
            "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call('del',KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";

    @SuppressWarnings("rawtypes")
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置键与值(添加)
     *
     * @param key   键
     * @param value 值
     */
    @Override
    public boolean set(final String key, final String value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    /**
     * 设置键、值、生存时间(添加)
     *
     * @param key     键
     * @param value   值
     * @param seconds 生存时间(按秒计算)
     */
    @Override
    public boolean setex(final int dbIndex, String key, final String value,
                         final int seconds) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }


    /**
     * 获取指定的key对应的值(查询)
     *
     * @param key 键
     * @return 值
     */
    @Override
    public String get(final String key) {
        String result = null;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            result = (String) operations.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    /**
     * 删除指定的键
     *
     * @param key 键
     * @return 状态码
     */
    @Override
    public boolean del(final String key) {
        boolean result = false;
        try {
            if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
                result = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean del(final int dbIndex, final String key) {
        boolean result = false;
        try {
            if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
                result = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean sadd(String key, String value) {
        boolean result = false;
        try {
            SetOperations<Serializable, Object> operations = redisTemplate.opsForSet();
            operations.add(key, value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean srem(String key, String value) {
        boolean result = false;
        try {
            SetOperations<Serializable, Object> operations = redisTemplate.opsForSet();
            operations.remove(key);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }


    @Override
    public Integer scard(String key) {
        Long result = null;
        try {
            RedisSerializer serializer = redisTemplate.getStringSerializer();
            byte[] keyByte = serializer.serialize(key);
            SetOperations<Serializable, Object> operations = redisTemplate.opsForSet();
            result = operations.size(keyByte);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result.intValue();
    }

    @Override
    public String spop(String key) {
        String result = null;
        try {
            SetOperations<Serializable, Object> operations = redisTemplate.opsForSet();
            result = (String) operations.pop(key);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    /**
     * 设置自增长的键（）
     *
     * @param key 键
     * @return 自增长值
     */
    @Override
    public Long incr(final String key) {
        Long result = null;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            result = operations.increment(key, 1);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }


    @Override
    public boolean hset(final String cachename, final String key, final String value) {
        boolean result = false;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            operations.put(cachename, key, value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean hmset(final int dbIndex,final String cachename, final Map map,final int second) {
        boolean result = false;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            operations.putAll(cachename, map);
            redisTemplate.expire(cachename, second, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean expire(String key, int seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 判断键是否存在
     *
     * @param key   键
     */
    @Override
    public boolean exists(final String key) {
        boolean result = false;
        try {
            result = (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    return redisConnection.exists(key.getBytes());
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage());
        }finally {
            close();
        }
        return result;
    }

    @Override
    public String hget(final String cachename, final String key) {
        String result = null;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            result = (String) operations.get(cachename, key);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }


    @Override
    public Map<String, String> hgetAll(final String cachename) {
        Map result = null;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            result = operations.entries(cachename);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    /**
     * 递增hash的value值
     *
     * @param cachename 缓存的名字；
     * @param key       缓存对应的hash key值
     * @return
     */
    @Override
    public boolean hincrByKey(final String cachename, String key) {
        boolean result = false;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            operations.increment(cachename, key, 1);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    /**
     * 递减hash的value值
     *
     * @param cachename 缓存的名字；
     * @param key       缓存对应的hash key值
     * @return
     */
    @Override
    public boolean hdimiByKey(final String cachename, String key) {
        boolean result = false;
        try {
            HashOperations<Serializable, String, Object> operations = redisTemplate.opsForHash();
            operations.increment(cachename, key, -1);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    @Override
    public boolean flush(int dbIndex) {
        return (boolean) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.select(dbIndex);
                redisConnection.flushDb();
                return true;
            }
        });
    }

    @Override
    public boolean hashKey(String cachename, String key) {
        return false;
    }

    /**
     * 释放连接
     */
    private void close(){
        RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
    }


    /**
     * 设置键与值(添加)
     *
     * @param key   键
     * @param value 值
     */
    @Override
    public boolean setIfAbsent(final String key, final String value,final int second) {
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            if (operations.setIfAbsent(key, value)){
                redisTemplate.expire(key,second,TimeUnit.SECONDS);
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close();
        }
        return false;
    }

    @Override
    public boolean acquire(String key, String value, long second) {
        logger.warn("[redis.acquire] key : {} , value : {} ",key,value);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void release(String key, String value) {
        logger.warn("[redis.release] key : {} , value : {} ",key,value);
        try {
            redisTemplate.execute(new DefaultRedisScript<>(COMPARE_AND_DELETE, String.class),
                    Collections.singletonList(key), value);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.warn("[redis.release] key : {} , finally. " ,key);
            close();
        }
    }
}
