package cn.dbj.domain.feed.eventHandel;

import cn.dbj.framework.starter.common.domain.event.DomainEventConsumer;
import cn.dbj.framework.starter.common.domain.event.consume.DomainEventListener;
import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.events.PostPublishEvent;
import cn.dbj.framework.starter.common.toolkit.MyObjectMapper;
import cn.dbj.framework.starter.common.tracing.MyTracingService;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostPublishEventListener implements DomainEventListener {
    private final MyObjectMapper myObjectMapper;
    private final DomainEventConsumer domainEventConsumer;
    private final MyTracingService myTracingService;
    @KafkaListener(topics = "PostPublishEvent",groupId = "postFeed")
    public void onMessage(ConsumerRecord<String,String> record){
        ScopedSpan scopedSpan = myTracingService.startNewSpan("domain-event-listener");
        Optional<?> message = Optional.ofNullable(record.value());
        // 0. 实际场景在消费MQ的时候，可以通过固定标识判断是否已经消费过，添加记录。对程序的处理会起到优化作用。
        // 1. 判断消息是否存在
        log.info("接收到消息"+record.value());
        if (!message.isPresent()) {
            return;
        }
        log.info("成功接收到消息");
        // 2. 转化对象（或者你也可以重写Serializer<T>）
        /*myObjectMapper.readValue(record.value() , CountChangeEvent.class);*/
        PostPublishEvent postPublishEvent = myObjectMapper.readValue(record.value() , PostPublishEvent.class);


        /*DomainEvent domainEvent = myObjectMapper.readValue(jsonString, DomainEvent.class);*/
        try {
            domainEventConsumer.consume(postPublishEvent);
        } catch (Throwable t) {
            log.error("Failed to listen domain event[{}:{}].", postPublishEvent.getType(), postPublishEvent.getId(), t);
        }

        scopedSpan.end();

    }

}

