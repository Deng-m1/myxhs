package cn.dbj.framework.starter.common.domain.event;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface DomainEventDao {
    void insert(List<DomainEvent> events);

    DomainEvent byId(String id);

    List<DomainEvent> byIds(List<String> ids);

    <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass);

    boolean updateConsumed(DomainEvent event);

    void successPublish(DomainEvent event);

    void failPublish(DomainEvent event);

    void successConsume(DomainEvent event);

    void failConsume(DomainEvent event);

    List<DomainEvent> tobePublishedEvents(String startId, int limit);
}
