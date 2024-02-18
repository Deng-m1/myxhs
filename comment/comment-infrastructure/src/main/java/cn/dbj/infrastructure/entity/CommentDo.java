package cn.dbj.infrastructure.entity;

import cn.dbj.domain.comment.model.valobj.CommentType;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
@Data
@Document(collection = "comment")
public class CommentDo {
    /**
     * 评论ID
     */
    private String id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private String postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
     */
    private String parentId;

    /**
     * 评论类型，参考CommentType枚举
     */
    private CommentType commentType;

    /**
     * 父评论用户ID
     */
    private Long parentUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 删除标志，0表示未删除，1表示已删除
     */
    private Integer deleted;

    /**
     * 评论类型字符串，用于持久化，不推荐使用
     */
    private String CommentType;

    /**
     * 图片URL，如果评论类型是图片评论时使用
     */
    private String imageUrl;


    // Getters and Setters

}
