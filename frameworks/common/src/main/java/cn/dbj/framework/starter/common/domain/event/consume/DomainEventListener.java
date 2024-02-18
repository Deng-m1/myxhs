package cn.dbj.framework.starter.common.domain.event.consume;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventConsumer;
import cn.dbj.framework.starter.common.toolkit.MyObjectMapper;
import cn.dbj.framework.starter.common.tracing.MyTracingService;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;


/*@RequiredArgsConstructor*/
public interface DomainEventListener  {
    /*private final MyObjectMapper myObjectMapper;
    private final DomainEventConsumer domainEventConsumer;
    private final MyTracingService myTracingService;

    public void onMessage(ObjectRecord<String, String> message) {
    }*/
}
