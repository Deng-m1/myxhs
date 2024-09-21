package cn.dbj.domain.comment.service.impl;

import cn.dbj.domain.comment.factory.CommentFactory;
import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.repository.ICommentRepository;
import cn.dbj.domain.comment.service.CommentCommandService;
import cn.dbj.framework.starter.designpattern.chain.AbstractChainContext;
import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import cn.dbj.types.command.LikeCommentCommand;
import cn.dbj.types.command.SimpleCommentResponse;
import cn.dbj.types.enums.CommentMarkEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCommandServiceImpl implements CommentCommandService {
    private final ICommentRepository commentRepository;

    private final AbstractChainContext<CreateCommentCommand> CreateCommentCommandAbstractChainContext;


    @Transactional
    @Override
    public CreateCommentResponse createCommentToComment(CreateCommentCommand command)
    {
        CreateCommentCommandAbstractChainContext.handler(CommentMarkEnum.COMMENT_TEXT_CONTENT.name(),command);
        Comment commentToComment = CommentFactory.createCommentToComment(command);
        commentRepository.saveCommentToComment(commentToComment);
        return new CreateCommentResponse(commentToComment.getId());


    }
    @Transactional
    @Override
    public CreateCommentResponse createCommentToPost(CreateCommentCommand command)
    {
        CreateCommentCommandAbstractChainContext.handler(CommentMarkEnum.COMMENT_TEXT_CONTENT.name(),command);
        Comment commentToPost = CommentFactory.createCommentToPost(command);
        commentRepository.saveCommentToPost(commentToPost);
        return new CreateCommentResponse(commentToPost.getId());

    }

    @Transactional
    @Override
    public SimpleCommentResponse createCommentLike(LikeCommentCommand command)
    {

        return new SimpleCommentResponse();

    }

}
