package cn.dbj.consumer;

import com.alibaba.fastjson.JSON;
import cn.dbj.model.Do.CountDo;
import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class FollowerConsumer {
    private Logger logger = LoggerFactory.getLogger(FollowerConsumer.class);


    private final BlurCounterService blurCounterService;

    @KafkaListener(topics = "follower-count-topic")
    public void onMessage(ConsumerRecord<String,String> record){
        CountDo counter = JSON.parseObject(record.value() , CountDo.class);
        //set数据
        System.out.println("测试ka成功");
        blurCounterService.setCounter(
                counter.getUid(),
                counter.getObjId(),
                counter.getCountKey() , counter.getCountValue());
    }

}
