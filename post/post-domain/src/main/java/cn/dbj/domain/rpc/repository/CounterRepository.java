/**
 * 仓储服务
 * 1. 定义仓储接口，之后由基础设施层做具体实现
 */
package cn.dbj.domain.rpc.repository;

import cn.dbj.domain.rpc.count.PostLikeCount;

import java.util.List;

public interface CounterRepository<T>
{

    T getCount(String tid, Long uid);


    List<T> getCountList(List<String> tid, Long uid);
}