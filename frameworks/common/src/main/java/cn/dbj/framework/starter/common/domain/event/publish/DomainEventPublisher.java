package cn.dbj.framework.starter.common.domain.event.publish;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<String> eventIds);
}