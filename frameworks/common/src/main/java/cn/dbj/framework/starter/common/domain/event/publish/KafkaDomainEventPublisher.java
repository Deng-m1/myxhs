package cn.dbj.framework.starter.common.domain.event.publish;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventJobs;
import cn.dbj.framework.starter.common.threadpool.build.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;



@Slf4j
@Component
@Profile("!ci")
@RequiredArgsConstructor
@Primary
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final KafkaDomainEventSender kafkaDomainEventSender;

    @Override
    public void publish(List<String> eventIds) {
        if (isNotEmpty(eventIds)) {
            List<DomainEvent> domainEvents = kafkaDomainEventSender.get(eventIds);
            for (DomainEvent event:domainEvents) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(event.getClass());
                        kafkaDomainEventSender.send(event);
                    }
                });
            }
        }
    }
}
