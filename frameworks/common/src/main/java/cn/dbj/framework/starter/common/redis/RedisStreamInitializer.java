package cn.dbj.framework.starter.common.redis;

import cn.dbj.framework.starter.common.properties.MyRedisProperties;

import io.lettuce.core.RedisBusyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import static cn.dbj.framework.starter.common.Constant.MyConstants.*;


@Slf4j
@Component("redisStreamInitializer")
@ConditionalOnProperty(name = "my.redis.use", havingValue = "true")
public class RedisStreamInitializer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MyRedisProperties myRedisProperties;

    public RedisStreamInitializer(RedisTemplate<String, Object> redisTemplate, MyRedisProperties myRedisProperties) {
        this.redisTemplate = redisTemplate;
        this.myRedisProperties = myRedisProperties;
        ensureConsumerGroupsExist();
    }

    private void ensureConsumerGroupsExist() {
        StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();
        tryCreateConsumerGroup(operations, myRedisProperties.getDomainEventStream(), REDIS_DOMAIN_EVENT_CONSUMER_GROUP);
        tryCreateConsumerGroup(operations, myRedisProperties.getWebhookStream(), REDIS_WEBHOOK_CONSUMER_GROUP);
        tryCreateConsumerGroup(operations, myRedisProperties.getNotificationStream(), REDIS_NOTIFICATION_CONSUMER_GROUP);
    }

    private void tryCreateConsumerGroup(StreamOperations<String, Object, Object> operations, String streamKey, String group) {
        try {
            operations.createGroup(streamKey, group);
            log.info("Created redis consumer group[{}].", group);
        } catch (RedisSystemException ex) {
            var cause = ex.getRootCause();
            if (cause != null && RedisBusyException.class.equals(cause.getClass())) {
                log.warn("Redis stream group[{}] already exists, skip.", group);
            } else {
                throw ex;
            }
        }
    }

}
