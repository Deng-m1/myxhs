package cn.dbj.domain.feed.model.factory;

import cn.dbj.domain.feed.model.aggregate.PostFeedReceiver;
import cn.dbj.domain.follow.service.RelationQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.List;

@RequiredArgsConstructor
public class PostFeedReceiverFactory {
    @DubboReference
    public RelationQueryService relationQueryService;

    public PostFeedReceiver initFeed(Long uid)
    {
        //用戶登陸查看
        List<Long> AttentionsList = relationQueryService.queryUserAttentionList(uid);
        return null;

    }
}
