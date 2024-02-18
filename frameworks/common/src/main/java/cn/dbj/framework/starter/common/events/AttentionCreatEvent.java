package cn.dbj.framework.starter.common.events;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;


import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@TypeAlias("ATTENTION_CREAT_EVENT")
@Getter
@NoArgsConstructor(access = PRIVATE)
public class AttentionCreatEvent extends DomainEvent implements Serializable {
    public Long userId;
    public Long attentionId;
    public AttentionCreatEvent(Long userId,Long attentionId)
    {
        super(DomainEventType.AttentionCreatEvent);
        this.userId=userId;
        this.attentionId=attentionId;
        /*System.out.println("AttentionCreatEvent"+userId+attentionId+this.getId());*/
    }
}
