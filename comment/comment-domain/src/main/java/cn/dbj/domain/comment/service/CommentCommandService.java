package cn.dbj.domain.comment.service;

import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import cn.dbj.types.command.LikeCommentCommand;
import cn.dbj.types.command.SimpleCommentResponse;
import org.springframework.transaction.annotation.Transactional;

public interface CommentCommandService {
    @Transactional
    CreateCommentResponse createCommentToComment(CreateCommentCommand command);

    @Transactional
    CreateCommentResponse createCommentToPost(CreateCommentCommand command);

    @Transactional
    SimpleCommentResponse createCommentLike(LikeCommentCommand command);
}
