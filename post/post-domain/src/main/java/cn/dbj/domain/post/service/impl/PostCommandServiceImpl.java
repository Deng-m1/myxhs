package cn.dbj.domain.post.service.impl;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.factory.PostFactory;
import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.repository.IPostRepository;
import cn.dbj.domain.post.service.PostCommandService;
import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.exception.ErrorCode;
import cn.dbj.framework.starter.common.exception.MyException;
import cn.dbj.framework.starter.designpattern.chain.AbstractChainContext;
import cn.dbj.model.CountDTO;
import cn.dbj.service.BlurCounterService;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import cn.dbj.types.enums.PostChainMarkEnum;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PostCommandServiceImpl implements PostCommandService {
    private final IPostRepository postMongoRepository;
    private final PostFactory postFactory;
    @DubboReference
    public UserService userService;
    @DubboReference
    public BlurCounterService blurCountService;




    private final AbstractChainContext<PostingReqBody> postingReqBodyAbstractChainContext;

    @Override
    @Transactional
    public void publishPost(PostingReqBody postingReqBody) {
        postingReqBodyAbstractChainContext.handler(PostChainMarkEnum.LOCAL_TEXT_CONTENT.name(),postingReqBody);
        Post post = postMongoRepository.query(postingReqBody.getTid());
        if (!post.getStatus().equals(PostStatus.WAIT_POSTED))
        {
            throw new MyException(ErrorCode.DEPARTMENT_NAME_DUPLICATES,"你的操作过快");
        }
        //userService.identifyHotspotUser(postingReqBody.getUserId());
        //判断是不是大v
        log.info("判断是不是大v",postingReqBody.getHotUser());

        CountDTO counter = blurCountService.getCounter(1L, postingReqBody.getUserId().toString(), RedisKeyConstant.ATTENTIONS_NUMBER);
        if (counter.getCountValue()!=null && counter.getCountValue()>1000L|| JdHotKeyStore.isHotKey(RedisKeyConstant.USER_QUERY))
        {
            postingReqBody.setHotUser(true);
        }
        System.out.println("判断是不是大v"+postingReqBody.getHotUser());


        post.publish(postingReqBody.getTitle(),postingReqBody.getSourceContent(),postingReqBody.getTopic(),postingReqBody.getHotUser()
                    ,postingReqBody.getPostType(),postingReqBody.getMainShowUrl(),postingReqBody.getUrl());

        postMongoRepository.save(post);


    }

    @Override
    @Transactional
    public PostingRespBody creatPost(PostingReqBody postingReqBody)
    {
        Long uid=postingReqBody.getUserId();

        Post post = postFactory.creatPost(uid);

        postMongoRepository.save(post);

        PostingRespBody postingRespBody = new PostingRespBody();
        postingRespBody.setPostId(post.getId());

        return postingRespBody;

    }


    @Override
    @Transactional
    public void likePost(String postId, Long uid) {
        Post post = postMongoRepository.query(postId);
        post.like(uid);
        postMongoRepository.likePost(post, uid);
    }





}
