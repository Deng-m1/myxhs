package cn.dbj.domain.post.service;

import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import org.springframework.transaction.annotation.Transactional;

public interface PostCommandService{

    public void publishPost(PostingReqBody postingReqBody);

    PostingRespBody creatPost(PostingReqBody postingReqBody);


    @Transactional
    void likePost(String postId, Long uid);
}