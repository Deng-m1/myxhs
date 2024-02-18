package cn.dbj.domain.comment.factory;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.model.valobj.CommentType;
import cn.dbj.domain.comment.repository.ICommentRepository;
import cn.dbj.framework.starter.common.exception.ErrorCode;
import cn.dbj.framework.starter.common.exception.MyException;
import cn.dbj.framework.starter.distributedid.core.snowflake.Snowflake;
import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class CommentFactory {
    private static ICommentRepository commentRepository;

    public CommentFactory(ICommentRepository commentRepository)
    {
        CommentFactory.commentRepository =commentRepository;
    }


    public static Comment createCommentToComment(CreateCommentCommand command)
    {
        if (!commentRepository.exitsComment(command.getParentId()))
        {
            throw new MyException(ErrorCode.EMPTY_ATTRIBUTE_FIXED_VALUE,"该评论不存在");
        }
        //TODO 还有很多判断
        Snowflake snowflake = new Snowflake(13);
        String id = snowflake.nextIdStr();
        if (command.getImageUrl()!=null)
        {       // 上传图片
                // 保存图片地址
            Comment newComment = new Comment(
                    id,
                    command.getUserId(),
                    command.getPostId(),
                    command.getContent(),
                    command.getParentId(),
                    CommentType.PICTURE,
                    command.getParentUserId(),
                    command.getTargetUserId(),
                    new Date(), // 创建时间
                    0, // 默认未删除
                    command.getImageUrl()
            );
            return newComment;
        }else {
            Comment newComment = new Comment(
                    id,
                    command.getUserId(),
                    command.getPostId(),
                    command.getContent(),
                    command.getParentId(),
                    CommentType.TEXT,
                    command.getParentUserId(),
                    command.getTargetUserId(),
                    new Date(), // 创建时间
                    0, // 默认未删除
                    null

            );
            return newComment;
        }
    }
    public static Comment createCommentToPost(CreateCommentCommand command)
    {
        Snowflake snowflake = new Snowflake(13);
        String id = snowflake.nextIdStr();
        if (command.getImageUrl()!=null)
        {       // 上传图片
            // 保存图片地址
            Comment newComment = new Comment(
                    id,
                    command.getUserId(),
                    command.getPostId(),
                    command.getContent(),
                    CommentType.PICTURE,
                    command.getTargetUserId(),
                    new Date(), // 创建时间
                    0, // 默认未删除
                    command.getImageUrl()
            );
            return newComment;
        }else {
            Comment newComment = new Comment(
                    id,
                    command.getUserId(),
                    command.getPostId(),
                    command.getContent(),
                    CommentType.TEXT,
                    command.getTargetUserId(),
                    new Date(), // 创建时间
                    0, // 默认未删除
                    command.getImageUrl()
            );
            return newComment;
        }
    }
}
