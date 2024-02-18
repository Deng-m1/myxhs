package cn.dbj.framework.starter.common.domain.event;

import cn.dbj.framework.starter.common.domain.constant.DomainEventStatus;
import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.distributedid.toolkit.SnowflakeIdUtil;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;

import static cn.dbj.framework.starter.common.Constant.MyConstants.EVENT_COLLECTION;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AttentionCreatEvent.class, name = "AttentionCreatEvent"),
        @JsonSubTypes.Type(value = FollowCreatEvent.class, name = "FollowCreatEvent")
}
)*/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
/*@TypeAlias("cn.dbj.framework.starter.common.domain.event.DomainEvent")*/
@Document(EVENT_COLLECTION)
@Getter
@NoArgsConstructor(access = PUBLIC)
public abstract class DomainEvent implements Serializable {
    private String id;//事件ID，不能为空
    /*private String arTenantId;//事件对应的租户ID，不能为空*/
    /*private String arId;//事件对应的聚合根ID，不能为空*/
    private DomainEventType type;//事件类型
    private DomainEventStatus status;//状态
    /*@TableField("published_count")*/
    private Integer publishedCount;//已经发布的次数，无论成功与否
    /*@TableField("consumed_count")*/
    private Integer consumedCount;//已经被消费的次数，无论成功与否
    /*private String raisedBy;//引发该事件的memberId*/
    /*@TableField("raised_at")*/
    private Instant raisedAt;//事件产生时间


   protected DomainEvent(DomainEventType type) {
        requireNonNull(type, "Domain event type must not be null.");
        /*requireNonNull(id, "id must not be null.");*/

        this.id = newEventId();
        this.type = type;
        this.status = DomainEventStatus.CREATED;
        this.publishedCount = 0;
        this.consumedCount = 0;
        /*this.raisedBy = user.getMemberId();*/
        this.raisedAt = now();
    }
    public String newEventId() {
        return "EVT" +SnowflakeIdUtil.nextId();
    }

    /*public String newEventId() {
        SnowflakeIdUtil snowflakeIdUtil
        return "EVT" +
    }

    public void setArInfo(AggregateRoot ar) {

    }

    public boolean isConsumedBefore() {
        return this.consumedCount > 0;
    }

    public boolean isNotConsumedBefore() {
        return !isConsumedBefore();
    }

    /*public boolean isRaisedByHuman() {
        return isNotBlank(raisedBy);
    }*/

}
