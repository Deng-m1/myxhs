package cn.dbj.domain.follow.repository;

import cn.dbj.domain.follow.model.entity.Attention;

import java.util.List;

public interface IAttentionRepository {
    public void setAttention(Attention attention);
    public Long getAttentionSum(Long uid);
    public List<Long> getAttentionList(Long uid);

    boolean existsRecord(Long userId, Long attAttentionId);
    public void save(Attention attention);
}
