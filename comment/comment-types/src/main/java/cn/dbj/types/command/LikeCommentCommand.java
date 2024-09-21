package cn.dbj.types.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeCommentCommand {
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
     * 评论ID
     */

    @NotNull(message = "评论ID不能为空")
    private String commentId;


    @NotNull(message = "点赞标识不能为空")
    private boolean like;


}
