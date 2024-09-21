package cn.dbj.framework.starter.common.domain;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@Getter
public abstract class AggregateRoot {
    private static final int MAX_OPS_LOG_SIZE = 20;

    private String id;//通过Snowflake算法生成

    private List<DomainEvent> events;//领域事件列表，用于临时存放完成某个业务流程中所发出的事件，会被BaseRepository保存到事件表中


    protected AggregateRoot() {
        this.clearEvents();
    }

    protected AggregateRoot(String id) {
      /*  requireNonNull(user, "User must not be null.");
        requireNonBlank(user.getTenantId(), "Tenant ID must not be blank.");
*/
        this.id = id;

    }


    protected void raiseEvent(DomainEvent event) {
        /*event.setArInfo(this);*/
        allEvents().add(event);
    }

    public void clearEvents() {
        this.events = null;
    }

    private List<DomainEvent> allEvents() {
        if (events == null) {
            this.events = new ArrayList<>();
        }

        return events;
    }

    protected void setId(String id) {
        this.id=id;
    }



}