/**
 * 仓储服务
 * 1. 定义仓储接口，之后由基础设施层做具体实现
 */
package cn.dbj.domain.comment.repository;


import cn.dbj.domain.comment.model.aggregate.Comment;

import java.util.List;

public interface ICommentRepository {


    List<Comment> queryCommentListByPostIdAndLastCommentId(String postId, int size, int page, String lastCommentId);

    List<Comment> queryCommentListWithoutCache(String postId, int size, int page, String lastCommentId);

    List<Comment> queryCommentListWithCache(String postId, int size, int page, String lastCommentId);

    void saveCommentToComment(Comment commentToComment);

    void saveCommentToPost(Comment commentToComment);

    boolean exitsComment(String parentId);
}