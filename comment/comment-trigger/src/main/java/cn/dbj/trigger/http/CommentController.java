/**
 * HTTP 接口服务
 */
package cn.dbj.trigger.http;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.service.CommentCommandService;
import cn.dbj.domain.comment.service.CommentQueryService;
import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentCommandService postCommandService;

    @Autowired
    private CommentQueryService commentQueryService;

    @PostMapping("/creat/post")
    @Transactional
    public Result<CreateCommentResponse> publishPost(@RequestBody CreateCommentCommand CreateCommentCommand) {
        CreateCommentResponse commentToPost = postCommandService.createCommentToPost(CreateCommentCommand);
        return Results.success(commentToPost);
    }

    @PostMapping("/creat/comment")
    @Transactional
    public Result<CreateCommentResponse> createPost(@RequestBody CreateCommentCommand CreateCommentCommand) {
        CreateCommentResponse commentToPost = postCommandService.createCommentToComment(CreateCommentCommand);
        return Results.success(commentToPost);
    }

    @GetMapping("/post/queryPage")
    public Result<List<Comment>> queryCommentListByPostIdAndLastCommentId(@RequestParam String postId, @RequestParam int size, @RequestParam int page, @RequestParam String lastCommentId) {
        List<Comment> comments = commentQueryService.queryCommentListByPostIdAndLastCommentId(postId, size, page, lastCommentId);
        return Results.success(comments);
    }

    @GetMapping("/post/queryPageWithoutCache")
    public Result<List<Comment>> queryCommentListWithoutCache(@RequestParam String postId, @RequestParam int size, @RequestParam int page, @RequestParam String lastCommentId) {
        List<Comment> comments = commentQueryService.queryCommentListWithoutCache(postId, size, page, lastCommentId);
        return Results.success(comments);
    }

    @GetMapping("/post/queryPageWithCache")
    public Result<List<Comment>> queryCommentListWithCache(@RequestParam String postId, @RequestParam int size, @RequestParam int page, @RequestParam String lastCommentId) {
        List<Comment> comments = commentQueryService.queryCommentListWithCache(postId, size, page, lastCommentId);
        return Results.success(comments);
    }
}