package cn.dbj.service.impl;

import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.toolkit.NullOrZeroUtils;
import cn.dbj.model.CountDTO;
import cn.dbj.model.Do.CountDo;
import cn.dbj.service.AccurateCounterService;
import cn.dbj.mapper.CounterMapper;
import cn.dbj.model.Counter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.RequiredArgsConstructor;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.COUNT_INFO;


/**
 * 精准计数
 * 精准计数实现起来就非常简单了，因为精准计数都是业务表中count计算，计数这边只需要在redis当中存储count就行了
 */
@Service
@RequiredArgsConstructor
@DubboService
public class AccurateCounterServiceImpl implements AccurateCounterService {


    private final RedisTemplate redisTemplate;


    private final CounterMapper counterMapper;

    //热数据缓存过期时间
    private final Long CACHE_EXPIRE_TIME = 12L;

    //缓存前缀
    private final String COUNT_PREFIX = "accurate_counter:";


    private final DistributedCache distributedCache;

    /**
     * 获取计数
     *
     * @param uid
     * @param objId
     * @param key
     * @return
     */
    @Override
    public CountDTO getCounter(Long uid, String objId, String key) {

        String ckey=COUNT_INFO + objId + key + uid;
        /*Long counterValue = distributedCache.safeGet(ckey,Long.class,
                ()->0L,1,
                TimeUnit.DAYS);*/
        Long counterValue = (Long)redisTemplate.opsForValue().get(ckey);
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果不为空直接返回就ok了
            return new CountDTO(uid,  objId , key , counterValue);
        }
        //查数据库
        LambdaQueryWrapper<CountDo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CountDo::getUid,uid).eq(CountDo::getObjId,objId).eq(CountDo::getCountKey,key);
        counterValue = counterMapper.selectOne(lambdaQueryWrapper).getCountValue();

        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果是热点数据，就保存到redis当中
            if(JdHotKeyStore.isHotKey(ckey)){
                redisTemplate.opsForValue().set(ckey,counterValue);
            }
            //如果不为空直接返回就ok了
            return new CountDTO(uid,  objId , key , counterValue);
        }
        //如果不为空直接返回就ok了
        return null;
    }

    /**
     * 写入计数
     *
     * @param uid
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @Override
    public CountDTO setCounter(Long uid, String objId, String key, Long value) {
        return null;
    }

    /**
     * 删除计数
     *
     * @param objId
     * @param objType
     * @param key
     * @return
     */
    @Override
    public CountDTO delCounter(String objId, String objType, String key) {
        return null;
    }

    /**
     * 获取计数
     *
     * @param objId
     * @param objType
     * @param key
     * @return
     */


    /*@Override
    public Counter getCounter(Integer objId, Integer objType , String key) {
        Integer counterValue = (Integer)redisTemplate.opsForValue().get(COUNT_PREFIX + key +":"+objType+":" + objId);
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果不为空直接返回就ok了
            return  new Counter(objId , key , counterValue);
        }
        counterValue = counterMapper.getCounter(objId, objType, key);
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果是热点数据，就保存到redis当中
            if(JdHotKeyStore.isHotKey("counter:"+key + ":" + objId)){
                redisTemplate.opsForValue().set(COUNT_PREFIX + key +":"+objType+":" + objId , counterValue);
            }
            //如果不为空直接返回就ok了
            return new Counter(objId , key , counterValue);
        }
        //如果不为空直接返回就ok了
        return null;
    }

    *//**
     * 修改缓存
     * @param objId
     * @param key
     * @param value
     * @return
     *//*
    @Override
    public Counter setCounter(Integer objId, Integer objType , String key, Integer value) {
        try {
            redisTemplate.opsForValue().set(COUNT_PREFIX + key +":"+objType+":" + objId , value);
            return null;
        }catch (Exception e){
            return  null;
        }

    }

    *//**
     * 删除缓存
     * @param objId
     * @param key
     * @return
     *//*
    @Override
    public Counter delCounter(Integer objId , Integer objType , String key) {
        try {
            redisTemplate.delete(COUNT_PREFIX + key +":"+objType+":" + objId);
            return null;
        }catch (Exception e){
            return null;
        }
    }*/
}
