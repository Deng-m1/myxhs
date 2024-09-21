package cn.dbj.domain.post.service;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.framework.starter.common.toolkit.Page;
import cn.dbj.types.dto.post.PostQueryRequest;

import java.util.List;
import java.util.Map;

public interface PostQueryService {
    Post queryPostDetail(String postId);

    Page queryUserPostList(PostQueryRequest queryRequest);

    boolean queryPostIdsByUserId(Long userId, String postId);

    Map<String, Boolean> queryPostIdsByUserIdList(Long userId, List<String> postIds);


    List<String> getPostListIds();

    List<String> queryPostListIdsByUserId(Long uid);
}
