/**
 * HTTP 接口服务
 */
package cn.dbj.trigger.http;

import cn.dbj.domain.comment.service.CommentCommandService;
import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentCommandService postCommandService;

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
}