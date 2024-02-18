/**
 * 仓储服务
 * 1. 定义仓储接口，之后由基础设施层做具体实现
 */
package cn.dbj.domain.follow.repository;

import cn.dbj.domain.follow.model.entity.Follow;

import java.util.List;

public interface IFollowRepository{
    public void testR();
    public void setFollower(Follow follow);
    public Long getFollowSum(Long uid);
    public List<Long> getFollowList(Long uid);

    boolean existsRecord(Long userId, Long followerId);

    public void save(Follow follow);
}