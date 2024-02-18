package cn.dbj.domain.post.model.factory;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.repository.IPostRepository;
import cn.dbj.framework.starter.designpattern.chain.AbstractChainContext;
import cn.dbj.framework.starter.distributedid.toolkit.SnowflakeIdUtil;
import cn.dbj.types.dto.post.PostingReqBody;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostFactory {
    private final IPostRepository postRepository;

    public Post creatPost(Long author)
    {
        //TODO 跨服务调用检查是否存在authorid
        /*String authorString = String.format("%08d", author % 100000000);*/
        String tid = SnowflakeIdUtil.nextIdStr();
        /*long l = (SnowflakeIdUtil.nextId() << 8) + (author % 256);
        String tid=Long.toString(l);*/
        Post post = new Post(tid,author);
        return post;
    }

    public void publishPost(PostingReqBody postingReqBody)
    {

    }

}
