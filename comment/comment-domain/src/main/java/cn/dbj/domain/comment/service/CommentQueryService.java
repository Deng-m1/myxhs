package cn.dbj.domain.comment.service;

import cn.dbj.domain.comment.model.aggregate.Comment;

import java.util.List;

public interface CommentQueryService {
    List<Comment> queryCommentListByPostIdAndLastCommentId(String postId, int size, int page, String lastCommentId);

    List<Comment> queryCommentListWithoutCache(String postId, int size, int page, String lastCommentId);

    List<Comment> queryCommentListWithCache(String postId, int size, int page, String lastCommentId);
}
