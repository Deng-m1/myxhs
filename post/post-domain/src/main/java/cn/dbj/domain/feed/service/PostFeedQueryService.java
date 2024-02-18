package cn.dbj.domain.feed.service;

import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;

import java.util.List;

public interface PostFeedQueryService {
    List<PostFeedBaseInfo> queryPostFollowFeed(Long uid, Long lastTime, Boolean reflash);
}
