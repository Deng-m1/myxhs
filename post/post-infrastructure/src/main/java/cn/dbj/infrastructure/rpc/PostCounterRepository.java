package cn.dbj.infrastructure.rpc;



import cn.dbj.domain.rpc.count.PostLikeCount;
import cn.dbj.domain.rpc.repository.CounterRepository;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.infrastructure.translator.PostLikeTranslator;
import cn.dbj.model.CountDTO;
import cn.dbj.service.BlurCounterService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostCounterRepository implements CounterRepository<PostLikeCount> {
    @DubboReference
    private BlurCounterService blurCounterService;


    @Override
    public PostLikeCount getCount(String tid, Long uid)
    {
        CountDTO countDTO = blurCounterService.getCounter(uid, tid, RedisKeyConstant.POST_LIKE);

        return PostLikeTranslator.INSTANCE.toPostLikeCount(countDTO);

    }

    @Override
    public List<PostLikeCount> getCountList(List<String> tid, Long uid) {
        List<CountDTO> counters = blurCounterService.getCounters(uid, RedisKeyConstant.POST_LIKE, tid);
        return counters.stream().map(PostLikeTranslator.INSTANCE::toPostLikeCount).toList();
    }

}
