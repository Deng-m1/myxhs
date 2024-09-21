/**
 * 聚合对象；
 * 1. 聚合实体和值对象
 * 2. 聚合是聚合的对象，和提供基础处理对象的方法。但不建议在聚合中引入仓储和接口来做过大的逻辑。而这些复杂的操作应该放到service中处理
 * 3. 对象名称 XxxAggregate
 */
package cn.dbj.domain.comment.model.aggregate;


import cn.dbj.domain.comment.model.entity.UserBaseInfo;
import cn.dbj.domain.comment.model.valobj.CommentType;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import cn.dbj.framework.starter.common.events.CreatCommentToPostEvent;
import lombok.*;

import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

/**
 * 评论聚合，用于表示用户对帖子的评论。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends AggregateRoot {


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
     * 评论内容
     */
    private String content;

    /**
     * 父评论的唯一标识符
     */
    private String parentId;

    /**
     * 父评论用户的唯一标识符
     */
    private Long parentUserId;

    /**
     * 删除状态：0表示未删除，1表示已删除
     */
    private Integer deleted;

    /**
     * 评论创建时间
     */
    private Date createTime;

    /**
     * 评论类型
     */
    private CommentType commentType;

    /**
     * 评论用户基本信息
     */
    private UserBaseInfo userBaseInfo;

    /**
     * 评论的图片链接
     */
    private String imageUrl;

    /**
     * 点赞数
     */
    private Long likeCount;
    //创建评论的评论
    public Comment(String id, Long userId, String postId, String content, String parentId, CommentType type, Long parentUserId, Long targetUserId, Date date, int deleted,String url) {
        super(id);
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.parentId = parentId;
        this.commentType = type;
        this.parentUserId = parentUserId;
        this.targetUserId = targetUserId;
        this.createTime = date;
        this.deleted = deleted;
        this.imageUrl=url;

    }

    public Comment(String id, Long userId, String postId, String content, CommentType type, Long targetUserId, Date date, int i, String imageUrl) {
        super(id);
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.parentId = "0";
        this.commentType = type;
        this.parentUserId = 0L;
        this.targetUserId = targetUserId;
        this.createTime = date;
        this.deleted = i;
        this.imageUrl=imageUrl;
        this.raiseEvent(new CreatCommentToPostEvent(id, userId, postId, targetUserId, parentId, parentUserId));
    }
}