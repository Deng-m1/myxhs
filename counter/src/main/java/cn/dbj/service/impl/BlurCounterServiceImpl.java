package cn.dbj.service.impl;

import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.toolkit.NullOrZeroUtils;
import cn.dbj.model.CountDTO;
import cn.dbj.mapper.CounterMapper;
import cn.dbj.model.Counter;
import cn.dbj.model.Do.CountDo;
import cn.dbj.service.BlurCounterService;
import cn.dbj.utils.RedisLock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.benmanes.caffeine.cache.*;
import com.jd.platform.hotkey.client.ClientStarter;
import com.jd.platform.hotkey.client.Context;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.jd.platform.hotkey.client.core.rule.KeyRuleHolder;
import com.jd.platform.hotkey.client.etcd.EtcdConfigFactory;
import com.jd.platform.hotkey.common.configcenter.ConfigConstant;
import com.jd.platform.hotkey.common.configcenter.IConfigCenter;
import com.jd.platform.hotkey.common.rule.KeyRule;
import com.jd.platform.hotkey.common.tool.FastJsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

import org.apache.dubbo.config.annotation.DubboService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.COUNT_INFO;

/**
 * 模糊计数
 */
@Service
@RequiredArgsConstructor
@DubboService
public class BlurCounterServiceImpl implements BlurCounterService {

    private Logger logger = LoggerFactory.getLogger(BlurCounterServiceImpl.class);

    public final DistributedCache distributedCache;


    private final ThreadPoolTaskExecutor customThreadPoolTaskExecutor;

    //冷数据添加到缓存过期时间
    private final Long DATABASE_EXPIRE_TIME = 2L;

    //热数据缓存过期时间
    private final Long CACHE_EXPIRE_TIME = 8L;

    //缓冲区阈值
    private final Integer BUFF_COUNT = 1000;

    private final String COUNT_PREFIX = "counter_";


    private final RedisLock lock;


    private final RedisTemplate redisTemplate;

    private final KafkaTemplate kafkaTemplate;

    private final RedissonClient redissonClient;

    @Resource
    private CounterMapper counterMapper;
    //当设置caffeine过期时间和redis过期时间一致时候，当缓存失效的时候，caffeine已经更新数据库成功了
    //不会出现查询数据库时候，还有一部分在caffeine里
    //创建缓冲区,为了方便，使用Caffeine当缓冲区
    Cache<String, Long> cache;

    @Value("${spring.application.name}")
    private String appName;



    @PostConstruct
    public void initHotkey() {
        /*ClientStarter.Builder builder = new ClientStarter.Builder();
        // 注意，setAppName很重要，它和dashboard中相关规则是关联的。
        ClientStarter starter = builder.setAppName("user")
                .setEtcdServer("http://127.0.0.1:12379")
                .setCaffeineSize(10)
                .build();
        starter.startPipeline();
        // 添加以下代码
        IConfigCenter configCenter = EtcdConfigFactory.configCenter();
        String rules = configCenter.get(ConfigConstant.rulePath + Context.APP_NAME);
        List<KeyRule> ruleList = FastJsonUtils.toList(rules, KeyRule.class);
        KeyRuleHolder.putRules(ruleList);*/
        cache=Caffeine.newBuilder()
                //设置写缓存后8秒钟过期
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .scheduler(Scheduler.systemScheduler())

                //设置缓存移除通知
                .removalListener((String keys, Object value, RemovalCause cause) -> {
                    //缓存移除，直接修改数据库
                    //COUNT_INFO + objId + objType + key + uid
                    if (cause==RemovalCause.EXPLICIT||cause==RemovalCause.EXPIRED)
                    {
                        kafkaTemplate.send("counter-topic",keys+":"+value);

                        /*String[] parts = keys.split(":"); // 拆分成数组
                        String objId = parts[1];
                        String key=parts[2];
                        Long uid = Long.parseLong(parts[3]);

                        counterMapper.setCounter(uid, objId,key+":" , (Long) value);*/

                    }

                    //异步发送kafka
                    // 发送消息到 Kafka 消息队列中
//                String message = key+":"+value;
//                kafkaTemplate.send("counter-topic", message);
                })
                //build方法可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
                .build(key -> {
                    return 0L; // 初始化值为0
                });

    }

   /* private void sendMsg(Long uid , String key , Long isDelete , String topic){
        CountDo counter=new CountDo();
        counter.setObjId(uid);
        counter.setCountKey(key);
        counter.setCountValue(isDelete);

        kafkaTemplate.send(topic, JSON.toJSONString(counter));
    }*/

    //单个值查询，比如查询浏览量
    /*@Override
    public Counter getCounter(Long objId , Integer objType, String key) {

        try {
            boolean hotKey = JdHotKeyStore.isHotKey("counter:"+key + ":"+objType+":" + objId);
            //查询缓存
            Integer countValue = (Integer)redisTemplate.opsForValue().get("counter:"+key + ":"+objType+":" + objId);
            if (!NullOrZeroUtils.isNullOrEmptyOrZero(countValue)) {
                //获取key过期时间
                Long expire = redisTemplate.opsForValue().getOperations().getExpire("counter:"+key + ":"+objType+":" + objId);
                //判断过期时间是否小于1小时
                if (expire<60*60){
                    //增加过期时间，不过这里QPS*2了，其实效率是比较低的了
                    redisTemplate.expire(key + ":" + objId , CACHE_EXPIRE_TIME , TimeUnit.HOURS);
                }
                //如果不为空直接返回就ok了
                return new Counter(objId , key , countValue);
            }
            //如果countValue为空就说明缓存中是没有数据的，所以这里就需要去查询数据库
            //判断是否是热点数据，如果是热点数据，为了防止突发流量打到数据库，需要加一个分布式锁
            if (hotKey){
                if (lock.lock("lock:"+key + ":"+objType+":" + objId , "1")){
                    //查询数据库
                    countValue = counterMapper.getCounter(objId,objType, key);
                    redisTemplate.opsForValue().set("counter:"+key + ":"+objType+":" + objId , countValue , DATABASE_EXPIRE_TIME , TimeUnit.HOURS);
                }else{
                    //获取锁失败的，可以直接返回获取失败稍后再试，或者while循环让他访问数据库
                    //这里使用while获取数据
                    while (true){
                        countValue = (Integer)redisTemplate.opsForValue().get("counter:"+key + ":"+objType+":" + objId);
                        if (countValue!=null){
                            break;
                        }
                    }
                }
            }else{
                //查询数据库
                countValue = counterMapper.getCounter(objId, objType, key);
            }
            return new Counter(objId , key , countValue);
        }catch (Exception e){

            return null;
        }finally {
            lock.unlock("lock:"+key + ":"+objType+":" + objId , "1");
        }
    }*/

    /**
     * 获取单个计数
     *
     *
     * @param key
     * @return
     */
    @Override
    public CountDTO getCounter(Long uid , String objId , String key) {
        String ckey = COUNT_INFO + objId + key + uid;
        Long counterValue = 0L;
        try {
            boolean hotKey = JdHotKeyStore.isHotKey(ckey);
            //查询缓存
            counterValue= (Long)redisTemplate.opsForValue().get(ckey);
            if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)) {
                /*//获取key过期时间
                Long expire = redisTemplate.opsForValue().getOperations().getExpire(ckey);
                //判断过期时间是否小于1小时
                if (expire<60*60){
                    //增加过期时间，不过这里QPS*2了，其实效率是比较低的了
                    redisTemplate.expire(key + ":" + objId , CACHE_EXPIRE_TIME , TimeUnit.HOURS);
                }
                //如果不为空直接返回就ok了*/
                return new CountDTO(uid,  objId , key , counterValue);
            }
            //如果countValue为空就说明缓存中是没有数据的，所以这里就需要去查询数据库
            //判断是否是热点数据，如果是热点数据，为了防止突发流量打到数据库，需要加一个分布式锁
            if (hotKey){
                counterValue = distributedCache.safeGet(ckey,
                        Long.class,
                ()-> {
                    LambdaQueryWrapper<CountDo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(CountDo::getUid,uid).eq(CountDo::getObjId,objId).eq(CountDo::getCountKey,key);
                    Long value = counterMapper.selectOne(lambdaQueryWrapper).getCountValue();
                    return value;
                },1,
                TimeUnit.DAYS);
            }else{
                //查询数据库
                LambdaQueryWrapper<CountDo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(CountDo::getUid,uid).eq(CountDo::getObjId,objId).eq(CountDo::getCountKey,key);
                counterValue = counterMapper.selectOne(lambdaQueryWrapper).getCountValue();
            }
            return new CountDTO(uid,  objId , key , counterValue);
        }catch (Exception e){

            new CountDTO(uid,  objId , key , counterValue);
        }


        return new CountDTO(uid,  objId , key , counterValue);
    }

    /**
     * 写入单个计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @Override
    public CountDTO setCounter(Long uid ,String objId, String key, Long value) {
        StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
        /*Boolean bool = instance.hasKey(COUNT_INFO + objId + key + uid);*/
        /*if (JdHotKeyStore.isHotKey(COUNT_INFO + objId + key + uid))
        {*/
        if(true){
            instance.opsForValue().increment(COUNT_INFO + objId + key + uid, value);
            //这里需要一个缓冲区来更新到数据库当中
            //添加到缓冲区中，批量更新数据库
            //logger.info(COUNT_INFO + objId + key + uid +"   ：----------是热key");
            putBuffer(uid ,objId , key , value);
        }else{
            //添加到数据库当中
            //没有缓存证明不是热点数据
            counterMapper.setCounter(uid, objId, key, value);
        }
        return null;
    }

    /**
     * 批量获取counter
     *

     * @return
     */
    @Override
    public List<CountDTO> getCounters(Long uid, String key, List<String> objIds) {
        /*List<String> ckeys = objIds.stream().map(objId -> COUNT_INFO + objId + key + uid).toList();
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String k : ckeys) {
                connection.stringCommands().get(k.getBytes());
            }
            return null;
        });

        // 处理结果
        List<String> values = results.stream().map(o -> (byte[]) o)
                .map(bytes -> bytes != null ? new String(bytes) : null)
                .toList();*/
        List<CompletableFuture<CountDTO>> futures = new ArrayList<>();

        for (String objId : objIds) {
            CompletableFuture<CountDTO> future = CompletableFuture.supplyAsync(() -> getCounter(uid, objId, key), customThreadPoolTaskExecutor);
            futures.add(future);
        }

        // 等待所有异步任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 收集所有完成的结果
        CompletableFuture<List<CountDTO>> allResults = allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));

        // 阻塞直到所有任务完成并返回结果
        return allResults.join();
    }

    /**
     * 获取多个计数
     *
     * @param uid
     * @param key
     * @param objId
     * @return
     */
    @Override
    public List<CountDTO> getCounters(List<Long> uid, String key, String objId) {
        List<CompletableFuture<CountDTO>> futures = new ArrayList<>();

        for (Long userId : uid) {
            CompletableFuture<CountDTO> future = CompletableFuture.supplyAsync(() -> getCounter(userId, objId, key), customThreadPoolTaskExecutor);
            futures.add(future);
        }

        // 等待所有异步任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 收集所有完成的结果
        CompletableFuture<List<CountDTO>> allResults = allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));

        // 阻塞直到所有任务完成并返回结果
        return allResults.join();
    }

    /**
     * 批量写入数据
     *
     * @param objId
     * @param objType
     * @param kv
     * @return
     */
    @Override
    public CountDTO setCounters(String objId, String objType, Map<String, Integer> kv) {
        return null;
    }

    /**
     * 写入数据库
     *
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @Override
    public CountDTO setCounterDB(String objId, String key, Integer value) {
        return null;
    }

    /**
     * 添加缓冲区
     * @param uid
     * @param objId
     * @param key
     * @param value
     */
    private void putBuffer(Long uid, String objId, String key, Long value){
        String cKey=COUNT_INFO + objId +":"+ key + uid;
        //key不存在则初始化0值
        /*RLock lock = redissonClient.getLock(cKey);
        if (lock.tryLock())
        {
            try {
                logger.info("获取到锁"+uid);
                Long countValue = cache.getIfPresent(cKey);
                if (countValue!=null)
                {
                    *//*ConcurrentMap<@NonNull String, @NonNull Long> stringLongConcurrentMap = cache.asMap();
                    for (Map.Entry<@NonNull String, @NonNull Long> entry : stringLongConcurrentMap.entrySet()) {
                        String key1 = entry.getKey();
                        Long value1 = entry.getValue();
                        // 执行你的操作，例如打印键值对
                        System.out.println("Key: " + key1 + ", Value: " + value1);
                    }*//*
                    cache.put(cKey , countValue +value);
                    if (countValue+value>=BUFF_COUNT){
                        logger.info("开始清楚数据");
                        cache.invalidate(COUNT_INFO + objId + objType + key + uid);
                        logger.info("结束清楚数据");
                    }
                }else {
                    cache.put(COUNT_INFO + objId + objType + key + uid , value);
                }

                logger.info("countValue==="+countValue);
                //值大于阈值，过期并执行监听器方法
            }  finally {
                lock.unlock();
            }
        }else {
            logger.info("未获取到锁，失败。。。。。。。。。。。");
        }*/
        ConcurrentMap<@NonNull String, @NonNull Long> stringLongConcurrentMap = cache.asMap();
        Long aLong = stringLongConcurrentMap.compute(cKey, (k, v) -> (v == null) ? value : v + value);
        /*Long countValue = cache.getIfPresent(cKey);
        if (countValue!=null)
        {
                    *//*ConcurrentMap<@NonNull String, @NonNull Long> stringLongConcurrentMap = cache.asMap();
                    for (Map.Entry<@NonNull String, @NonNull Long> entry : stringLongConcurrentMap.entrySet()) {
                        String key1 = entry.getKey();
                        Long value1 = entry.getValue();
                        // 执行你的操作，例如打印键值对
                        System.out.println("Key: " + key1 + ", Value: " + value1);
                    }*//*
            cache.put(cKey , countValue +value);
            if (countValue+value>=BUFF_COUNT){
                logger.info("开始清楚数据");
                cache.invalidate(COUNT_INFO + objId + objType + key + uid);
                logger.info("结束清楚数据");
            }
        }else {
            cache.put(COUNT_INFO + objId + objType + key + uid , value);
        }*/
        if (aLong>=BUFF_COUNT){
            cache.invalidate(cKey);
        }
        /*Long countValue = (Long)cache.getIfPresent(COUNT_INFO + objId + objType + key + uid);
        //判断
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(countValue)){
            cache.put(COUNT_INFO + objId + objType + key + uid , countValue +value);
            if (countValue+value>=BUFF_COUNT){
                cache.invalidate(COUNT_INFO + objId + objType + key + uid);
            }
        }else{
            cache.put(COUNT_INFO + objId + objType + key + uid , value);
        }*/
    }



    //返回一个新的时间戳
    public long addHoursToTimestamp(long timestamp, long hours) {
        long hoursInMs = hours * 60 * 60 * 1000;
        return timestamp + hoursInMs;
    }

    //拼装数据
    private List<Counter> resultCounters(List<Object> result , Integer objId, List<String> keys){
        List<Counter> counters = new ArrayList<>();
        int i = 0;
        for (Object obj : result){
            if (i==0){
                i++;
                continue;
            }
            Counter counter = new Counter();
            counter.setCountKey(keys.get(i));
            counter.setObjId(objId);
            counter.setCountValue((Integer)obj);
            counters.add(counter);
            i++;
        }
        return counters;
    }


    /*@Override
    public Counter setCounterDB(Integer objId, String key, Integer value) {
        return null;
    }*/
}
