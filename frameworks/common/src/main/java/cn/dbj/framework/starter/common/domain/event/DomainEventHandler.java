package cn.dbj.framework.starter.common.domain.event;

import cn.dbj.framework.starter.common.toolkit.MyTaskRunner;

public interface DomainEventHandler {

    boolean canHandle(DomainEvent domainEvent);

    void handle(DomainEvent domainEvent, MyTaskRunner taskRunner);

    default int priority() {
        return 0;//越小优先级越高
    }

}