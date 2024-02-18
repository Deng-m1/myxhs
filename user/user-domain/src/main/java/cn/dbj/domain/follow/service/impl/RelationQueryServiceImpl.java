package cn.dbj.domain.follow.service.impl;

import cn.dbj.domain.follow.repository.IAttentionRepository;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.follow.service.RelationQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@DubboService
public class RelationQueryServiceImpl implements RelationQueryService {
    private final IFollowRepository followRepository;
    private final IAttentionRepository attentionRepository;
    @Override
    public List<Long> queryUserAttentionList(Long uid)
    {
        return attentionRepository.getAttentionList(uid);
    }
    @Override
    public List<Long> queryUserFollowList(Long uid)
    {
        return followRepository.getFollowList(uid);
    }
}
