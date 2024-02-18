/**
 * 仓储实现；用于实现 domain 中定义的仓储接口，如；IXxxRepository 在 Repository 中调用服务
 */
package cn.dbj.infrastructure.follow.repository;

import cn.dbj.domain.follow.model.entity.Attention;
import cn.dbj.domain.follow.repository.IAttentionRepository;

import cn.dbj.domain.userInfo.model.dto.CountDTO;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.mongodb.MongoBaseRepository;
import cn.dbj.framework.starter.common.mysql.MysqlBaseRepository;
import cn.dbj.infrastructure.follow.dao.AttentionDao;
import cn.dbj.infrastructure.follow.dao.CounterDao;
import cn.dbj.infrastructure.follow.entity.AttentionDo;
import cn.dbj.infrastructure.follow.entity.CountDo;
import cn.dbj.infrastructure.follow.entity.FollowerDo;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.*;


@Repository
@RequiredArgsConstructor
public class AttentionRepository extends MongoBaseRepository<Attention> implements IAttentionRepository {

    public static final String COUNT_KEY = "ATTENTIONS_NUMBER:";
    public static final String OBJ_TYPE = ":self:";
    public static final long OBJ_ID = 1L;
    public final DistributedCache distributedCache;
    public final KafkaTemplate kafkaTemplate;
    public final CounterDao counterDao;
    public final Redisson redisson;
    public final AttentionDao attentionDao;
    public DefaultRedisScript<Long> countHashScript;
    public DefaultRedisScript<Long> AttentionListScript;


    @PostConstruct
    public void init(){
        countHashScript = new DefaultRedisScript<>();
        countHashScript.setResultType(Long.class);
        countHashScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/CountHash.lua")));

        AttentionListScript = new DefaultRedisScript<>();
        //返回值为Long
        AttentionListScript.setResultType(Long.class);
        AttentionListScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/Attentions.lua")));


    }
    @Override
    public void save(Attention attention)
    {
        AttentionDo attentionDo = new AttentionDo();
        attentionDo.setUserId(attention.getUserId());
        attentionDo.setAttentionId(attention.getAttentionId());
        attentionDo.setDelFlag(attention.getIsDelete());
        saveAttention(attentionDo);
        super.save(attention);

        String key= ATTENTION_LIST + attention.getUserId().toString();
        String arg=attention.getAttentionId().toString();
        StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();

        if (attention.getIsDelete()==0)
        {
            instance.execute(AttentionListScript, Collections.singletonList(key),arg);
        }else {
            instance.delete(key);
        }


    }




    public void saveAttention(AttentionDo attentionDo) {
        if (attentionDo.getDelFlag()==1)
        {
            LambdaUpdateWrapper<AttentionDo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(AttentionDo::getUserId, attentionDo.getUserId())
                    .eq(AttentionDo::getAttentionId,attentionDo.getAttentionId());
            attentionDao.delete(lambdaUpdateWrapper);
        }else {
            attentionDao.insert(attentionDo);

        }
    }


    @Override
    @Transactional
    public void setAttention(Attention attention) {
        if(attention!=null)
        {
            CountDTO countDTO = CountDTO.builder()
                    .objId(OBJ_ID)
                    .uid(attention.getUserId())
                    .objType(OBJ_TYPE)
                    .countKey(COUNT_KEY)
                    .countValue(attention.getIsDelete()==0?1L:-1L)
                    .build();
            //TODO 理想情况是在网关层面进行转发到技术服务
            kafkaTemplate.send("count-change-topic", JSON.toJSONString(countDTO));
            //利用消息队列进行数据库削峰
            AttentionDo attentionDo = new AttentionDo();
            attentionDo.setUserId(attention.getUserId());
            attentionDo.setAttentionId(attention.getAttentionId());
            attentionDo.setDelFlag(attention.getIsDelete());
            /*attentionDao.insertOrUpdate(attentionDo);*/
            kafkaTemplate.send("attention-insert-topic", JSON.toJSONString(attentionDo));
            String key=ATTENTION_LIST + attention.getUserId().toString();
            String arg=attention.getAttentionId().toString();
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            if (Boolean.TRUE.equals(instance.hasKey(key)))
            {
                if (attention.getIsDelete()==0)
                {
                    instance.execute(AttentionListScript, Collections.singletonList(key),arg);
                }else {
                    instance.delete(key);
                }
            }
        }
    }

    @Override
    public Long getAttentionSum(Long uid) {
        Long result;
        result= distributedCache.safeGet(
                COUNT_INFO + OBJ_ID + COUNT_KEY + uid,
                Long.class,
                ()-> {
                    //TODO 从计数中心获取，而不是从数据库中获取
                    LambdaQueryWrapper<CountDo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(CountDo::getCountKey,COUNT_KEY)
                            .eq(CountDo::getUid,uid);;
                    return Optional.ofNullable(counterDao.selectOne(lambdaQueryWrapper))
                            .map(CountDo::getCountValue).orElse(0L);
                },
                1,
                TimeUnit.DAYS
        );
        return result==null?0:result;
    }

    @Override
    public List<Long> getAttentionList(Long uid) {
        //关注列表实时性较高，一般粉丝不会查看大v的粉丝，一般会看大v的关注，大v的关注列表并发量会很高，但可以设置hotkey过期时间长，因为无论正常人还是大v，
        //因为无论正常人还是大v，关注列表看的都会比粉丝列表多
        //hotkey的阈值设置高点，为大v准备的，普通人一般从缓存读
        boolean hotkey = JdHotKeyStore.isHotKey(ATTENTION_LIST + uid);
        List<Long> attentionList;

        if (hotkey) {
            attentionList = getAttentionFromLocalCache(uid);
        } else {
            attentionList = getAttentionFromRedis(uid);
        }

        if (attentionList == null || attentionList.size() == 0) {
            // If there is no data in Redis, fetch from the database
            attentionList = getAttentionFromDB(uid);
        }

        if (attentionList.size() == 0) {
            // If there is no data in the database either, return an empty list
            attentionList = new ArrayList<>();
        }

        return attentionList;
    }

    @Override
    public boolean existsRecord(Long userId, Long attAttentionId) {
        LambdaQueryWrapper<AttentionDo> eq = new LambdaQueryWrapper<>();
        eq.eq(AttentionDo::getUserId,userId).eq(AttentionDo::getAttentionId,attAttentionId).eq(AttentionDo::getDelFlag,0);
        return attentionDao.exists(eq);
    }

    private List<Long> getAttentionFromLocalCache(Long uid) {
        List<Long> attentions = (List<Long>) JdHotKeyStore.get(ATTENTION_LIST + uid);
        if (attentions != null) {
            return attentions;
        }

        // If there is no data in the local cache, fetch from Redis
        attentions = getAttentionFromRedis(uid);

        if (attentions == null) {
            // If there is no data in Redis, fetch from the database
            attentions = getAttentionFromDB(uid);
        }

        if (attentions != null) {
            // Write the data to the local cache
            JdHotKeyStore.smartSet(ATTENTION_LIST + uid, attentions);
        }

        return attentions;
    }

    private List<Long> getAttentionFromRedis(Long uid) {
        RedisTemplate instance = (RedisTemplate) distributedCache.getInstance();
        ListOperations<String, String> stringStringListOperations = instance.opsForList();
        List<String> JsonList = stringStringListOperations.range(ATTENTION_LIST + uid, 0, -1);
        if (JsonList!=null)
        {
            return JsonList.stream()
                    .map(Long::parseLong).collect(Collectors.toList());
        }
        return null;
    }

    private List<Long> getAttentionFromDB(Long uid) {
        // Query all attention_id where user_id is equal to uid
        LambdaQueryWrapper<AttentionDo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AttentionDo::getAttentionId).eq(AttentionDo::getUserId, uid)
                .orderByDesc(AttentionDo::getUpdateTime)
                .last("Limit 200");

        List<AttentionDo> attentions = attentionDao.selectList(lambdaQueryWrapper);
        List<Long> collect = attentions.stream()
                .map(AttentionDo::getAttentionId)
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            // If there is data in the database, write it to Redis
            RedisTemplate instance = (RedisTemplate) distributedCache.getInstance();
            ListOperations<String, String> stringStringListOperations = instance.opsForList();
            stringStringListOperations.leftPushAll(ATTENTION_LIST + uid, collect.stream().map(Object::toString).collect(Collectors.toList()));
        }

        return collect;
    }
}