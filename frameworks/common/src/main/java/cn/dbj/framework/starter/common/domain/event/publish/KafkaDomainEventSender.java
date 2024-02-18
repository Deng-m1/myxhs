package cn.dbj.framework.starter.common.domain.event.publish;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventDao;
import cn.dbj.framework.starter.common.toolkit.MyObjectMapper;


import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDomainEventSender {
    private final DomainEventDao domainEventDao;
    private final MyObjectMapper myObjectMapper;
    private final KafkaTemplate<String,String> kafkaTemplate;

    public List<DomainEvent> get(List<String> eventIds)
    {
        List<DomainEvent> domainEvents = domainEventDao.byIds(eventIds);
        return domainEvents;
    }

    public void send(DomainEvent event) {
        try {
            String type = event.getType().name();
            String s = JSON.toJSONString(event);
            String eventString = myObjectMapper.writeValueAsString(event);
            extracted(eventString,type);
            domainEventDao.successPublish(event);
        } catch (Throwable t) {
            log.error("Error happened while publish domain event[{}:{}] to redis.", event.getType(), event.getId(), t);
            domainEventDao.failPublish(event);
        }
    }


    private void extracted(String eventString,String topic)
    {
        kafkaTemplate.send(topic,eventString);
        log.info("成功发送------------"+topic);
    }
}
