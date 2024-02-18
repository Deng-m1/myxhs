package cn.dbj.infrastructure.user.repository;

import cn.dbj.domain.userInfo.repository.CounterRepository;
import cn.dbj.model.CountDTO;
import cn.dbj.service.BlurCounterService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository
public class UserCounterReposity implements CounterRepository {
    @DubboReference
    private BlurCounterService blurCounterService;


    @Override
    public Long getCount(Long uid, String Type)
    {
        CountDTO countDTO = blurCounterService.getCounter(uid, "1", Type);
        return countDTO.getCountValue();
    }

    @Override
    public Map<Long,Long> getCountList(List<Long> uids,String Type) {
        List<CountDTO> counters = blurCounterService.getCounters(uids, Type, "1");
        return counters.stream().collect(Collectors.toMap(CountDTO::getUid, CountDTO::getCountValue));
    }

}