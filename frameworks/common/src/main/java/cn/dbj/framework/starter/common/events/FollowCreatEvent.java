package cn.dbj.framework.starter.common.events;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.io.Serializable;


import static lombok.AccessLevel.PRIVATE;


@Getter
@TypeAlias("FOLLOW_CREAT_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class FollowCreatEvent extends DomainEvent implements Serializable {
    public Long userId;
    public Long followerId;
    public FollowCreatEvent(Long userId,Long followerId)
    {
        super(DomainEventType.FollowCreatEvent);
        this.userId=userId;
        this.followerId=followerId;
        System.out.println("followerCreatEvent"+userId+followerId+this.getId());
    }
}
