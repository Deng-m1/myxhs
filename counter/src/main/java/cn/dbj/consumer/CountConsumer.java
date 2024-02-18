package cn.dbj.consumer;



import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.toolkit.MyObjectMapper;

import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;




@Component
@RequiredArgsConstructor
public class CountConsumer {
    private Logger logger = LoggerFactory.getLogger(CountConsumer.class);

    private final BlurCounterService blurCounterService;
    private final MyObjectMapper myObjectMapper;

    /*@KafkaListener(topics = "CountChangeEvent")*/
    public void onMessage(ConsumerRecord<String,String> record){
        Optional<?> message = Optional.ofNullable(record.value());
        // 0. 实际场景在消费MQ的时候，可以通过固定标识判断是否已经消费过，添加记录。对程序的处理会起到优化作用。
        // 1. 判断消息是否存在
        logger.info("接收到消息"+record.value());
        if (!message.isPresent()) {
            return;
        }
        logger.info("成功接收到消息");
        // 2. 转化对象（或者你也可以重写Serializer<T>）
        /*myObjectMapper.readValue(record.value() , CountChangeEvent.class);*/
        CountChangeEvent countChangeEvent = myObjectMapper.readValue(record.value() , CountChangeEvent.class);

        //set数据
        blurCounterService.setCounter(
                countChangeEvent.getUid(),
                countChangeEvent.getObjId(),
                countChangeEvent.getCountKey() , countChangeEvent.getCountValue());
    }

}
