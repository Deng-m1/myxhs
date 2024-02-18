/**
 * HTTP 接口服务
 */
package cn.dbj.trigger.http;


import cn.dbj.domain.post.service.PostCommandService;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostCommandService postCommandService;

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
}