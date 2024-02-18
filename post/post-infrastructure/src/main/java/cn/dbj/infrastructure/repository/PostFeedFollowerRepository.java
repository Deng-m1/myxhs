package cn.dbj.infrastructure.repository;

import cn.dbj.domain.feed.repository.PostFeedRepository;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.toolkit.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class PostFeedFollowerRepository implements PostFeedRepository {
    private final RedisTemplate redisTemplate;

    @Override
    public void creatPostFeedReceiver(Long userId,List<Long> attentionsLists) {
        String key = RedisKeyConstant.POST_FEED_FOLLOW_RECEIVER;
        final Date curDate = new Date();
        final Date limitDate = DateUtil.addDateDays(curDate, -7);

        final Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(key + userId, -1, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            //获取收件箱最后更新的一个时间
            init(userId,oldTime.longValue(),new Date().getTime(),attentionsLists);
        } else {
            init(userId,limitDate.getTime(),curDate.getTime(),attentionsLists);
        }
    }


    public void init(Long userId, Long min, Long max, Collection<Long> attentionsLists) {
        String t1 = RedisKeyConstant.POST_FEED_FOLLOW_SENDER;
        String t2 = RedisKeyConstant.POST_FEED_FOLLOW_RECEIVER;
        // 查看关注人的发件箱
        final List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long attentionId : attentionsLists) {
                connection.zRevRangeByScoreWithScores((t1 + attentionId).getBytes(), min, max, 0, 50);
            }
            return null;
        });
        final ObjectMapper objectMapper = new ObjectMapper();
        final HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        final Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        final byte[] key = (t2 + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        });
    }
}
