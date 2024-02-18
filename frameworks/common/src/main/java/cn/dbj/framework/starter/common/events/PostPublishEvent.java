package cn.dbj.framework.starter.common.events;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.io.Serializable;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@TypeAlias("POST_PUBLISH_EVENT")
@Getter
@NoArgsConstructor(access = PRIVATE)
public class PostPublishEvent extends DomainEvent implements Serializable {
    private Boolean hotUser;
    private Long userId;
    private String postId;
    private Date publishTime;

    public PostPublishEvent(Boolean hotUser, Long userId, String postId, Date publishTime) {
        super(DomainEventType.PostPublishEvent);
        this.hotUser = hotUser;
        this.userId = userId;
        this.postId = postId;
        this.publishTime = publishTime;
    }
}
