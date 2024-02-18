/**
 * 仓储实现；用于实现 domain 中定义的仓储接口，如；IXxxRepository 在 Repository 中调用服务
 */
package cn.dbj.infrastructure.follow.repository;

import cn.dbj.domain.follow.model.entity.Attention;
import cn.dbj.domain.follow.model.entity.Follow;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.userInfo.model.dto.CountDTO;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.mongodb.MongoBaseRepository;
import cn.dbj.infrastructure.follow.dao.CounterDao;
import cn.dbj.infrastructure.follow.dao.FollowerDao;
import cn.dbj.infrastructure.follow.entity.CountDo;
import cn.dbj.infrastructure.follow.entity.FollowerDo;
import cn.dbj.infrastructure.user.entity.UserDo;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.*;

@Repository
@RequiredArgsConstructor
public class FollowRepository extends MongoBaseRepository<Follow> implements IFollowRepository{

    public static final String COUNT_KEY = "FOLLOWS_NUMBER:";
    public static final long OBJ_ID = 1L;
    public static final String OBJ_TYPE = ":self:";
    public final FollowerDao followerDao;
    public final DistributedCache distributedCache;
    public final KafkaTemplate kafkaTemplate;
    public final CounterDao counterDao;
    public final Redisson redisson;
    private final RedissonClient redissonClient;
    public DefaultRedisScript<Long> countHashScript;
    public DefaultRedisScript<Long> followerListScript;


    @PostConstruct
    public void init(){
        countHashScript = new DefaultRedisScript<>();
        countHashScript.setResultType(Long.class);
        countHashScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/CountHash.lua")));

        followerListScript = new DefaultRedisScript<>();
        //返回值为Long
        followerListScript.setResultType(Long.class);
        followerListScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/Followers.lua")));


    }
    @Override
    public void save(Follow follow)
    {
        FollowerDo followerDo= new FollowerDo();
        followerDo.setUserId(follow.getUserId());
        followerDo.setFollowerId(follow.getFollowerId());
        followerDo.setDelFlag(follow.getIsDelete());
        saveFollow(followerDo);
        super.save(follow);

        String key=FOLLOW_LIST + follow.getUserId().toString();
        String arg=follow.getFollowerId().toString();
        StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
        if (follow.getIsDelete()==0)
        {
                instance.execute(followerListScript, Collections.singletonList(key),arg);
        }else {
                instance.boundListOps(key);
        }


    }

    public void saveFollow(FollowerDo followerDo) {
        if (followerDo.getDelFlag()==1)
        {
            LambdaUpdateWrapper<FollowerDo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(FollowerDo::getUserId, followerDo.getUserId())
                    .eq(FollowerDo::getFollowerId,followerDo.getFollowerId());
            followerDao.delete(lambdaUpdateWrapper);
        }else {
            followerDao.insert(followerDo);
        }
    }


    @Override
    public void testR() {

        LambdaQueryWrapper<FollowerDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FollowerDo::getUserId, 1);

        List<FollowerDo> followers = followerDao.selectList(queryWrapper);
        System.out.println(followers.size());

    }
    /*
    关注某人
    * */

    @Override
    public void setFollower(Follow follow) {
        if(follow!=null)
        {

            CountDTO countDTO = CountDTO.builder()
                    .objId(OBJ_ID)
                    .uid(follow.getUserId())
                    .objType(OBJ_TYPE)
                    .countKey(COUNT_KEY)
                    //IsDelete()为0时候为未删除
                    .countValue(follow.getIsDelete()==0?1L:-1L)
                    .build();
            //TODO 理想情况是在网关层面进行转发到技术服务
            kafkaTemplate.send("count-change-topic",JSON.toJSONString(countDTO));
            //利用消息队列进行数据库削峰
            FollowerDo followerDo= new FollowerDo();
            followerDo.setUserId(follow.getUserId());
            followerDo.setFollowerId(follow.getFollowerId());
            followerDo.setDelFlag(follow.getIsDelete());
            kafkaTemplate.send("follower-insert-topic",JSON.toJSONString(followerDo));
            //TODO 更新粉丝列表缓存
            String key=ATTENTION_LIST + follow.getUserId().toString();
            String arg=follow.getFollowerId().toString();
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            if (Boolean.TRUE.equals(instance.hasKey(key)))
            {
                if (follow.getIsDelete()==0)
                {
                    instance.execute(followerListScript, Collections.singletonList(key),arg);
                }else {
                    instance.boundListOps(key);
                }
            }
        }
        /*StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        //TODO 检查userId和followerId是否存在
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        //TODO 根据delete判断缓存加一还是减一，当key不存在时，还要先获取*/
        /*FollowerDo followerDo= new FollowerDo();
        followerDo.setUserId(follow.getUserId());
        followerDo.setFollowerId(follow.getFollowerId());
        followerDo.setDelFlag(follow.getIsDelete());*/



    }
    /*
    获取关注人数，采用key value模式
    * */
    @Override
    public Long getFollowSum(Long uid) {
        Long result=null;
        result= distributedCache.safeGet(
                COUNT_INFO + OBJ_ID + COUNT_KEY + uid,
                Long.class,
                ()-> {
                    LambdaQueryWrapper<CountDo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(CountDo::getCountKey,COUNT_KEY)
                            .eq(CountDo::getUid,uid);
                    return Optional.ofNullable(counterDao.selectOne(lambdaQueryWrapper))
                            .map(CountDo::getCountValue).orElse(0L);
                },
                1,
                TimeUnit.DAYS
        );
        return result==null?0:result;
        /*StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        //TODO 检查userId和followerId是否存在
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        //根据脚本查询是否存在key，如果存在且过期时间小于60s，则自动续期
        List<String> keys = List.of(COUNT_INFO + uid);
        List<String> args = List.of(FOLLOW_NUM);

        Long result = stringRedisTemplate.execute(countHashScript, keys, args);
        if(result==null)
        {
            if(JdHotKeyStore.isHotKey(COUNT_INFO + uid+ FOLLOW_NUM))
            {
                //如果是热key，请求分布式锁直接打到数据库上


            }




            }
        }*/


        /*if(hashOperations.hasKey(COUNT_INFO+uid,FOLLOW_NUM))
        {
            Long num= (Long) hashOperations.get(COUNT_INFO+uid,FOLLOW_NUM);


        }else {
            counterMapper.selectById(uid);
        }*/


    }

    @Override
    public List<Long> getFollowList(Long uid) {
        //粉丝列表实时性较低，一般粉丝不会查看大v的粉丝，一般会看大v的关注，大v的关注列表并发量会很高，
        boolean hotkey = JdHotKeyStore.isHotKey(FOLLOW_LIST+uid);
        List<Long> followList = new ArrayList<>();
        if (hotkey)
        {
            followList=getFollowersFromLocalCache(uid);
        }
        else {
            followList=getFollowersFromRedis(uid);
        }
        if (followList == null || followList.size() == 0) {
            // 如果 Redis 中没有数据，则从数据库中获取
            followList = getFollowersFromDB(uid);
        }

        if (followList.size() == 0) {
            // 如果数据库中也没有数据，则返回一个空的列表
            followList = new ArrayList<>();
        }

        return followList;
    }

    @Override
    public boolean existsRecord(Long userId, Long followerId) {
        LambdaQueryWrapper<FollowerDo> eq = new LambdaQueryWrapper<>();
        eq.eq(FollowerDo::getUserId,userId).eq(FollowerDo::getFollowerId,followerId).eq(FollowerDo::getDelFlag,0);
        return followerDao.exists(eq);
    }

    private List<Long> getFollowersFromLocalCache(Long uid) {
        List<Long> followers = (List<Long>) JdHotKeyStore.get(FOLLOW_LIST + uid);
        if (followers != null) {
            return followers;
        }
        // 如果本地缓存中没有数据，则从 Redis 中获取
        followers = getFollowersFromRedis(uid);
        if (followers == null) {
            followers = getFollowersFromDB(uid);
        }
        if (followers != null) {
            // 将数据写入本地缓存
            JdHotKeyStore.smartSet(FOLLOW_LIST + uid, followers);
        }
        return followers;
    }
    private List<Long> getFollowersFromRedis(Long uid) {
        RedisTemplate instance = (RedisTemplate) distributedCache.getInstance();
        ListOperations<String, String> stringStringListOperations = instance.opsForList();
        List<String> JsonList = stringStringListOperations.range(FOLLOW_LIST + uid, 0, -1);
        if (JsonList!=null)
        {
            return JsonList.stream()
                    .map(Long::parseLong).collect(Collectors.toList());
        }
        return null;
    }
    private List<Long> getFollowersFromDB(Long uid) {
        //查找所有user_id等于uid的follower_id
        LambdaQueryWrapper<FollowerDo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(FollowerDo::getFollowerId).eq(FollowerDo::getUserId,uid)
                .orderByDesc(FollowerDo::getUpdateTime)
                .last("Limit 200");
        List<FollowerDo> followers = followerDao.selectList(lambdaQueryWrapper);
        List<Long> collect = followers.stream()
                .map(FollowerDo::getFollowerId)
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            // 如果数据库中有数据，则将数据写入 Redis 中
            RedisTemplate instance = (RedisTemplate) distributedCache.getInstance();
            ListOperations<String, String> stringStringListOperations = instance.opsForList();
            stringStringListOperations.leftPushAll(FOLLOW_LIST + uid, collect.stream().map(Object::toString).collect(Collectors.toList()));
        }
        return collect;
    }
}