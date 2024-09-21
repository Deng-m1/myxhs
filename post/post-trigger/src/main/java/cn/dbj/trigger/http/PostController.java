/**
 * HTTP 接口服务
 */
package cn.dbj.trigger.http;


import cn.dbj.domain.post.service.PostCommandService;
import cn.dbj.domain.post.service.PostQueryService;
import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostCommandService postCommandService;

    @Autowired
    private PostQueryService postQueryService;

    @PostMapping("/publish")
    @Transactional
    public void publishPost(@RequestBody PostingReqBody postingReqBody) {
        postCommandService.publishPost(postingReqBody);
    }

    @PostMapping("/create")
    @Transactional
    public PostingRespBody createPost(@RequestBody PostingReqBody postingReqBody) {
        return postCommandService.creatPost(postingReqBody);
    }

    @GetMapping("/getPostList")
    public Result<List<String>> getPostList() {
        return Results.success(postQueryService.getPostListIds());
    }

    @GetMapping("/getPostListIdsByUserId")
    public Result<List<String>> getPostListIdsByUserId(@RequestParam Long uid) {
        return Results.success(postQueryService.queryPostListIdsByUserId(uid));
    }
}