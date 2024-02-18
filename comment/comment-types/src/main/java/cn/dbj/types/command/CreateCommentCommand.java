package cn.dbj.types.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
@Data
public class CreateCommentCommand {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private String postId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID
     */
    private String parentId;

    /**
     * 评论类型，参考CommentType枚举
     */
    private String commentType;

    /**
     * 父评论用户ID
     */
    private Long parentUserId;

    /**
     * 被评论的用户ID
     */

    private Long targetUserId;

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

    // 省略构造函数和getter/setter方法
}
