package cn.dbj.framework.starter.common.events;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.io.Serializable;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@TypeAlias("CREAT_COMMENT_TO_POST_EVENT")
@Getter
@NoArgsConstructor(access = PRIVATE)
public class CreatCommentToPostEvent extends DomainEvent implements Serializable {

    private String commentId;

    /**
     * 评论用户的唯一标识符
     */
    private Long userId;

    /**
     * 所评论帖子的唯一标识符
     */
    private String postId;

    /**
     * 所评论帖子的用户
     */
    private Long targetUserId;

    /**
     * 父评论的唯一标识符
     */
    private String parentId;

    /**
     * 父评论用户的唯一标识符
     */
    private Long parentUserId;



    public CreatCommentToPostEvent(String commentId, Long userId, String postId, Long targetUserId,
                                   String parentId, Long parentUserId) {
        super(DomainEventType.PosCreatCommentToPostEventtPublishEvent);
        this.commentId = commentId;
        this.userId = userId;
        this.postId = postId;
        this.targetUserId = targetUserId;
        this.parentId = parentId;
        this.parentUserId = parentUserId;

    }
}
