package cn.dbj.framework.starter.common.domain.event.publish;

import cn.dbj.framework.starter.common.domain.event.DomainEventJobs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/*
* 异步事件执行发布*/
@Slf4j
@Component
@Profile("!ci")
@RequiredArgsConstructor
public class AsynchronousDomainEventPublisher implements DomainEventPublisher {
    private final TaskExecutor taskExecutor;
    private final DomainEventJobs domainEventJobs;

    @Override
    public void publish(List<String> eventIds) {
        if (isNotEmpty(eventIds)) {
            taskExecutor.execute(domainEventJobs::publishDomainEvents);
        }
    }

}