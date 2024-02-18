package cn.dbj.domain.userInfo.repository;

import java.util.List;
import java.util.Map;

public interface CounterRepository
{


    Long getCount(Long uid, String Type);

    Map<Long,Long> getCountList(List<Long> uids, String Type);
}