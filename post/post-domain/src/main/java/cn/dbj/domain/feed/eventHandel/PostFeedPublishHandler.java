package cn.dbj.domain.feed.eventHandel;

import cn.dbj.domain.follow.service.RelationQueryService;
import cn.dbj.framework.starter.bases.ApplicationContextHolder;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.events.PostPublishEvent;
import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventHandler;
import cn.dbj.framework.starter.common.toolkit.MyTaskRunner;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static cn.dbj.framework.starter.common.domain.constant.DomainEventType.PostPublishEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostFeedPublishHandler implements DomainEventHandler {
    /*private final RedisTemplate redisTemplate;*/
    private final DistributedCache distributedCache;


    @DubboReference
    private UserService userService;
    @DubboReference
    private RelationQueryService relationQueryService;

    /*private RedisTemplate myRedisTemplate;

    @PostConstruct
    public void initr() {
        this.myRedisTemplate = ApplicationContextHolder.getBean(StringRedisTemplate.class);
    }*/


    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PostPublishEvent;
    }

    @Override
    public void handle(DomainEvent domainEvent, MyTaskRunner taskRunner) {
        RedisTemplate myRedisTemplate = (RedisTemplate) distributedCache.getInstance();
        log.info("PostPublishHandler handle");
        PostPublishEvent postPublishEvent = (PostPublishEvent) domainEvent;
        boolean hotkey=postPublishEvent.getHotUser();
        Long userId=postPublishEvent.getUserId();
        long currentTime = postPublishEvent.getPublishTime().getTime();
        // 获取用户ID的前八位并转换为小数部分
        double fraction = extractFractionFromUserId(userId);
        // 将时间戳作为整数部分，将小数部分和整数部分组合起来作为分数
        double score = currentTime + fraction;
        String postId = postPublishEvent.getPostId();

        if(hotkey){
            //推送到热门发件箱
            myRedisTemplate.opsForZSet().add(RedisKeyConstant.POST_FEED_FOLLOW_SENDER + userId,postId,score);
            log.info("推送到热门发件箱"+userId+" "+postId+" "+score);
            List<Long> longs = relationQueryService.queryUserFollowList(userId);
            for (Long uid:longs) {
                if (!ObjectUtils.isEmpty(uid)) {
                    myRedisTemplate.opsForZSet().add(RedisKeyConstant.POST_FEED_FOLLOW_RECEIVER + userId, postId, score);
                }
            }
            return ;
        }
        //推送到粉丝收件箱
        //TODO 重写函数如果是大V则返回活跃粉丝列表
        List<Long> longs = relationQueryService.queryUserFollowList(userId);
        myRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long uid:longs) {
                if (!ObjectUtils.isEmpty(uid)) {
                    byte[] key = (RedisKeyConstant.POST_FEED_FOLLOW_RECEIVER + uid).getBytes();
                    connection.zAdd(key, score, postId.getBytes());
                }
            }
            return null;
        });
        //先判断是不是大v，推送到粉丝收件箱

    }
    // 提取用户ID的前八位并转换为小数部分
    private double extractFractionFromUserId(Long userId) {
        // 将用户ID转换为字符串
        String userIdString = String.valueOf(userId);
        // 获取前八位字符
        String fractionPart = userIdString.substring(0, Math.min(8, userIdString.length()));
        // 转换为小数
        double fraction = Double.parseDouble(fractionPart) / 100000000.0;
        // 将前八位转换为小数部分
        return fraction;
    }

   /* private void publishWebhookEvent(SubmissionCreatedEvent theEvent) {
        tenantRepository.cachedByIdOptional(theEvent.getArTenantId()).ifPresent(tenant -> {
            if (!tenant.isDeveloperAllowed()) {
                return;
            }

            appRepository.cachedByIdOptional(theEvent.getAppId()).ifPresent(app -> {
                if (!app.isWebhookEnabled()) {
                    return;
                }

                app.pageByIdOptional(theEvent.getPageId()).ifPresent(page -> {
                    if (page.submissionWebhookTypes().contains(ON_CREATE)) {
                        webhookEventPublisher.publish(theEvent);
                    }
                });
            });
        });
    }*/

}
