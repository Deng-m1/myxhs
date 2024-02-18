package cn.dbj.framework.starter.common.mongodb;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventDao;
import cn.dbj.framework.starter.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;



import java.util.List;

import static cn.dbj.framework.starter.common.domain.constant.DomainEventStatus.*;
import static cn.dbj.framework.starter.common.exception.ErrorCode.DOMAIN_EVENT_NOT_FOUND;
import static cn.dbj.framework.starter.common.toolkit.CommonUtils.requireNonBlank;
import static cn.dbj.framework.starter.common.toolkit.MapUtils.mapOf;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Slf4j
@Component
@RequiredArgsConstructor
@Primary
public class MongoDomainEventDao implements DomainEventDao {
    private final MongoTemplate mongoTemplate;

    @Override
    public void insert(List<DomainEvent> events) {
        requireNonNull(events, "Domain events must not be null.");
        log.info("正在插入事件数据");

        mongoTemplate.insertAll(events);
    }

    @Override
    public DomainEvent byId(String id) {
        requireNonBlank(id, "Domain event ID must not be blank.");

        DomainEvent domainEvent = mongoTemplate.findOne(query(where("_id").is(id)), DomainEvent.class);
        if (domainEvent == null) {
            throw new MyException(DOMAIN_EVENT_NOT_FOUND, "未找到领域事件。", mapOf("eventId", id));
        }

        return domainEvent;
    }

    @Override
    public List<DomainEvent> byIds(List<String> ids) {
        requireNonNull(ids, "Domain event IDs must not be null.");

        return mongoTemplate.find(query(where("_id").in(ids)).with(by(ASC, "raisedAt")), DomainEvent.class);
    }

    @Override
    public <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        requireNonBlank(arId, "AR ID must not be blank.");
        requireNonNull(type, "Domain event type must not be null.");
        requireNonNull(eventClass, "Domain event class must not be null.");

        Query query = query(where("arId").is(arId).and("type").is(type)).with(by(DESC, "raisedAt"));
        return mongoTemplate.findOne(query, eventClass);
    }
    @Override
    public boolean updateConsumed(DomainEvent event)
    {
        Query query = new Query(Criteria.where("_id").is(event.getId()).and("status").is(PUBLISH_SUCCEED));
        Update update = new Update().set("status", CONSUMING);
        // 执行条件更新操作
        return mongoTemplate.updateFirst(query, update, DomainEvent.class).wasAcknowledged();


    }

    @Override
    public void successPublish(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        Query query = Query.query(where("_id").is(event.getId()));
        Update update = new Update();
        update.set("status", PUBLISH_SUCCEED.name()).inc("publishedCount");
        mongoTemplate.updateFirst(query, update, DomainEvent.class);
    }

    @Override
    public void failPublish(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        Query query = Query.query(where("_id").is(event.getId()));
        Update update = new Update();
        update.set("status", PUBLISH_FAILED.name()).inc("publishedCount");
        mongoTemplate.updateFirst(query, update, DomainEvent.class);
    }

    @Override
    public void successConsume(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        Query query = Query.query(where("_id").is(event.getId()));
        Update update = new Update();
        update.set("status", CONSUME_SUCCEED.name()).inc("consumedCount");
        mongoTemplate.updateFirst(query, update, DomainEvent.class);
    }

    @Override
    public void failConsume(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        Query query = Query.query(where("_id").is(event.getId()));
        Update update = new Update();
        update.set("status", CONSUME_FAILED.name()).inc("consumedCount");
        mongoTemplate.updateFirst(query, update, DomainEvent.class);
    }



    @Override
    public List<DomainEvent> tobePublishedEvents(String startId, int limit) {
        requireNonBlank(startId, "Start ID must not be blank.");

        Query query = query(where("status").in("CREATED","PUBLISH_FAILED","CONSUME_FAILED")
                .and("_id").gt(startId)
                .and("publishedCount").lt(3)
                .and("consumedCount").lt(3))
                .with(by(ASC, "raisedAt"))
                .limit(limit);
        List<DomainEvent> domainEvents = mongoTemplate.find(query, DomainEvent.class);
        return domainEvents;
    }
}
