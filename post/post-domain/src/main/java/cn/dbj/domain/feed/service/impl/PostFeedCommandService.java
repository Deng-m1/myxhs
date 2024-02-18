package cn.dbj.domain.feed.service.impl;

import cn.dbj.domain.feed.repository.PostFeedRepository;
import cn.dbj.domain.feed.service.PostFeedCommendService;
import cn.dbj.domain.follow.service.RelationQueryService;
import cn.dbj.domain.userInfo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostFeedCommandService implements PostFeedCommendService {
    @DubboReference
    private UserService userService;
    @DubboReference
    private RelationQueryService relationQueryService;
    private final PostFeedRepository postFeedFollowerRepository;
    public void initPostFeedReceiver(Long uid) {
        //TODO 如果用户收件箱为空，先初始化用户收件箱
        List<Long> attentionsLists = relationQueryService.queryUserAttentionList(uid);
        postFeedFollowerRepository.creatPostFeedReceiver(uid,attentionsLists);
    }
}
