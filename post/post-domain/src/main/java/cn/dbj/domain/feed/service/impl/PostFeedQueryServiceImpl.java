package cn.dbj.domain.feed.service.impl;

import cn.dbj.domain.feed.repository.PostFeedRepository;
import cn.dbj.domain.feed.service.PostFeedQueryService;
import cn.dbj.domain.follow.service.RelationQueryService;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.domain.post.repository.IPostRepository;
import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
@Service
@RequiredArgsConstructor
public class PostFeedQueryServiceImpl implements PostFeedQueryService {
    private final RedisTemplate redisTemplate;
    @DubboReference
    private UserService userService;
    @DubboReference
    private RelationQueryService relationQueryService;
    private final PostFeedRepository postFeedFollowerRepository;
    private final IPostRepository postRepository;


    @Override
    public List<PostFeedBaseInfo> queryPostFollowFeed(Long uid, Long lastTime, Boolean reflash)
    {
        //如果是刷新操作
        if(reflash)
        {
            initPostFeedReceiver(uid);
        }
        //TODO 如果用户收件箱为空，先初始化用户收件箱
        Set<String> personReceiver = redisTemplate.opsForZSet()
                .reverseRangeByScore(RedisKeyConstant.POST_FEED_FOLLOW_RECEIVER + uid,
                        0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);
        if (ObjectUtils.isEmpty(personReceiver)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            return Collections.EMPTY_LIST;
        }
        //TODO 判断用户是否点赞过改帖子
        Map<String, Boolean> postLikedByUserList = postRepository.isPostLikedByUserList(uid, personReceiver.stream().toList());
        //TODO 查询帖子信息
        List<PostFeedBaseInfo> postFeedBaseInfos = postRepository.queryList(personReceiver.stream().toList());
        postFeedBaseInfos.forEach(postFeedBaseInfo -> {
            postFeedBaseInfo.setUserLike(postLikedByUserList.get(postFeedBaseInfo.getId().toString()));
        });


        // 这里不会按照时间排序，需要手动排序
        return postFeedBaseInfos;
    }

    public void initPostFeedReceiver(Long uid) {
        //TODO 如果用户收件箱为空，先初始化用户收件箱
        List<Long> attentionsLists = relationQueryService.queryUserAttentionList(uid);
        postFeedFollowerRepository.creatPostFeedReceiver(uid,attentionsLists);
    }


}
