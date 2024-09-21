package cn.dbj.consumer;



import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.toolkit.MyObjectMapper;

import cn.dbj.mapper.CounterMapper;
import cn.dbj.service.BlurCounterService;
import jakarta.annotation.Resource;
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
    @Resource
    private CounterMapper counterMapper;
    @KafkaListener(topics = "counter-topic")
    public void onMessage(ConsumerRecord<String,String> record){
        Optional<?> message = Optional.ofNullable(record.value());
        // 0. 实际场景在消费MQ的时候，可以通过固定标识判断是否已经消费过，添加记录。对程序的处理会起到优化作用。
        // 1. 判断消息是否存在
        //logger.info("接收到消息"+record.value());
        if (!message.isPresent()) {
            return;
        }
        String keys=record.value();
        String[] parts = keys.split(":"); // 拆分成数组
        String objId = parts[1];
        String key=parts[2];
        Long uid = Long.parseLong(parts[3]);
        counterMapper.setCounter(uid, objId,key+":" , Long.parseLong(parts[4]));
        //logger.info("成功接收到消息");
        // 2. 转化对象（或者你也可以重写Serializer<T>）
        /*myObjectMapper.readValue(record.value() , CountChangeEvent.class);*/
    }

}
