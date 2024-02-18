package cn.dbj.domain.follow.service.impl;

import cn.dbj.domain.follow.model.entity.Follow;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.follow.service.FollowService;
import cn.dbj.domain.userInfo.model.dto.CountDTO;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@DubboService
public class FollowServiceImpl implements FollowService {


    public static final String COUNT_KEY = "FOLLOWS_NUMBER";
    public final IFollowRepository followRepository;
    public final KafkaTemplate kafkaTemplate;
    @Override
    public void test() {
       followRepository.testR();
        System.out.println("dasdasdasdd");
    }


    public Long getFollowSum(Long uid)
    {
        return followRepository.getFollowSum(uid);
    }

    @Override
    public void setFollower(Follow follow) {
        kafkaTemplate.send("follow-insert-topic", JSON.toJSONString(follow));
        CountDTO countDTO = CountDTO.builder()
                .objId(follow.getFollowerId())
                .uid(follow.getUserId())
                .objType("CHANGE")
                .countKey(COUNT_KEY)
                .countValue(follow.getFollowerId())
                .build();
        kafkaTemplate.send("count-change-topic",JSON.toJSONString(countDTO));
    }
}
