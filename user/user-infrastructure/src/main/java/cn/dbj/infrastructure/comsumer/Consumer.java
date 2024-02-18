/**
 * 监听服务；在单体服务中，解耦流程。类似MQ的使用，如Spring的Event，Guava的事件总线都可以。如果使用了 Redis 那么也可以有发布/订阅使用。
 * Guava：https://bugstack.cn/md/road-map/guava.html
 */
package cn.dbj.infrastructure.comsumer;

import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.userInfo.model.dto.CountDTO;
import cn.dbj.infrastructure.follow.dao.AttentionDao;
import cn.dbj.infrastructure.follow.dao.FollowerDao;
import cn.dbj.infrastructure.follow.entity.AttentionDo;
import cn.dbj.infrastructure.follow.entity.FollowerDo;
import cn.dbj.infrastructure.follow.repository.AttentionRepository;
import cn.dbj.infrastructure.follow.repository.FollowRepository;
import cn.dbj.infrastructure.user.dao.UserDao;
import cn.dbj.infrastructure.user.entity.UserDo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.ATTENTIONS_NUMBER;
import static cn.dbj.framework.starter.common.Constant.RedisKeyConstant.FOLLOWS_NUMBER;
import static cn.dbj.types.common.RedisKeyConstant.USER_INFO;

@Component
@RequiredArgsConstructor
@Transactional
public class Consumer {
    private Logger logger = LoggerFactory.getLogger(Consumer.class);

    public final IFollowRepository followRepository;
    public final FollowerDao followerDao;
    public final AttentionDao attentionDao;
    public final UserDao userDao;

    @KafkaListener(topics = "follower-insert-topic",groupId = "follower-topic")
    public void onFollowerMessage(ConsumerRecord<String,String> record){
        Optional<?> message = Optional.ofNullable(record.value());

        // 0. 实际场景在消费MQ的时候，可以通过固定标识判断是否已经消费过，添加记录。对程序的处理会起到优化作用。

        // 1. 判断消息是否存在
        if (!message.isPresent()) {
            return;
        }

        // 2. 转化对象（或者你也可以重写Serializer<T>）
        FollowerDo followerDo = JSON.parseObject((String) message.get(), FollowerDo.class);
        logger.info("消费MQ消息，异步扣减活动库存 message：{}", message.get());
        /*FollowRepository.saveFollow(followerDo);*/
        logger.info("消费MQ消息，设置粉丝列表成功 message：{}", message.get());
        // 3. 更新数据库库存【实际场景业务体量较大，可能也会由于MQ消费引起并发，对数据库产生压力，所以如果并发量较大，可以把库存记录缓存中，并使用定时任务进行处理缓存和数据库库存同步，减少对数据库的操作次数】

    }
    private boolean existsByUserIdAndAttentionId(Long userId, Long attentionId) {
        QueryWrapper<AttentionDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("attention_id", attentionId);

        return queryWrapper.nonEmptyOfEntity();
    }

    @KafkaListener(topics = "attention-insert-topic",groupId = "attention-topic")
    public void onAttentionMessage(ConsumerRecord<String,String> record){
        Optional<?> message = Optional.ofNullable(record.value());

        // 0. 实际场景在消费MQ的时候，可以通过固定标识判断是否已经消费过，添加记录。对程序的处理会起到优化作用。
        // 1. 判断消息是否存在
        if (!message.isPresent()) {
            return;
        }

        // 2. 转化对象（或者你也可以重写Serializer<T>）
        AttentionDo attentionDo = JSON.parseObject((String) message.get(), AttentionDo.class);
        logger.info("消费MQ消息，设置关注列表成功 message：{}", message.get());
        /*AttentionRepository.saveAttention(attentionDo);*/

        // 3. 更新数据库库存【实际场景业务体量较大，可能也会由于MQ消费引起并发，对数据库产生压力，所以如果并发量较大，可以把库存记录缓存中，并使用定时任务进行处理缓存和数据库库存同步，减少对数据库的操作次数】
        /*boolean bool =existsByUserIdAndAttentionId(attentionDo.getUserId(), attentionDo.getAttentionId());
        if(bool)
        {
            attentionDao.insert(attentionDo);
        }
        else {
            LambdaUpdateWrapper<AttentionDo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(AttentionDo::getUserId, attentionDo.getUserId())
                    .eq(AttentionDo::getAttentionId,attentionDo.getAttentionId())
                    .set(AttentionDo::getDelFlag,attentionDo.getDelFlag());
            attentionDao.update(attentionDo,lambdaUpdateWrapper);
        }*/

        /*attentionDao.insertOrUpdate(attentionDo);*/

    }

    @KafkaListener(topics = "user-count-update",groupId = "user-topic")
    public void onUserCountMessage(ConsumerRecord<String,String> record) {
        logger.info("开始消费用户点赞数更新");
        Optional<?> message = Optional.ofNullable(record.value());
        if (!message.isPresent()) {
            return;
        }
        // 2. 转化对象（或者你也可以重写Serializer<T>）
        CountDTO countDTO = JSON.parseObject((String) message.get(), CountDTO.class);
        UserDo userDo = new UserDo();
        userDo.setId(countDTO.getUid());
        /*userDo.setId(countDTO.getUid());*/
        LambdaUpdateWrapper<UserDo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserDo::getId,userDo.getId());
        if (Objects.equals(countDTO.getCountKey(), FOLLOWS_NUMBER))
        {
            lambdaUpdateWrapper.setSql("follower_count = follower_count + " + countDTO.getCountValue());
        } else if(Objects.equals(countDTO.getCountKey(), ATTENTIONS_NUMBER))
        {
            lambdaUpdateWrapper.setSql("attention_count = attention_count + " + countDTO.getCountValue());
        }
        userDao.update(userDo,lambdaUpdateWrapper);
        /*if (JdHotKeyStore.isHotKey(USER_INFO+countDTO.getUid()))
        {
            UserDo value = (UserDo) JdHotKeyStore.getValue(USER_INFO + countDTO.getUid());
            if (Objects.equals(countDTO.getCountKey(), FOLLOWS_NUMBER))
            {
                value.setFollowerCount(countDTO.getCountValue()+ value.getFollowerCount());
            } else if(Objects.equals(countDTO.getCountKey(), ATTENTIONS_NUMBER))
            {
                value.setAttentionCount(countDTO.getCountValue()+value.getAttentionCount());
            }

        }*/
    }


}