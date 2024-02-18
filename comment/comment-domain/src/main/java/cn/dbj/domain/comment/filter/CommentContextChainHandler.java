package cn.dbj.domain.comment.filter;

import cn.dbj.framework.starter.designpattern.chain.AbstractChainHandler;

import cn.dbj.types.enums.CommentMarkEnum;


public interface CommentContextChainHandler<CreateCommentCommand> extends AbstractChainHandler<cn.dbj.types.command.CreateCommentCommand> {

    @Override
    default String mark() {
        return CommentMarkEnum.COMMENT_TEXT_CONTENT.name();
    }
}
