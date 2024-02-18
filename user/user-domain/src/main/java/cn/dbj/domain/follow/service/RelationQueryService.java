package cn.dbj.domain.follow.service;

import java.util.List;

public interface RelationQueryService {
    List<Long> queryUserAttentionList(Long uid);

    List<Long> queryUserFollowList(Long uid);
}
