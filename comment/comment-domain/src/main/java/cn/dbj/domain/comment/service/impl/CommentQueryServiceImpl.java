package cn.dbj.domain.comment.service.impl;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.repository.ICommentRepository;
import cn.dbj.domain.comment.service.CommentQueryService;
import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryServiceImpl implements CommentQueryService {

    private final ICommentRepository commentRepository;

    @DubboReference
    private BlurCounterService blurCounterService;


    @Override
    public List<Comment> queryCommentListByPostIdAndLastCommentId(String postId, int size, int page, String lastCommentId) {
        return commentRepository.queryCommentListByPostIdAndLastCommentId(postId, size, page, lastCommentId);
    }

    @Override
    public List<Comment> queryCommentListWithoutCache(String postId, int size, int page, String lastCommentId) {
        return commentRepository.queryCommentListWithoutCache(postId, size, page, lastCommentId);
    }

    @Override
    public List<Comment> queryCommentListWithCache(String postId, int size, int page, String lastCommentId) {


        return commentRepository.queryCommentListWithCache(postId, size, page, lastCommentId);
    }






}
