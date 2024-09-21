package cn.dbj.domain.post.service.impl;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.domain.post.repository.IPostRepository;
import cn.dbj.domain.post.service.PostQueryService;
import cn.dbj.domain.rpc.count.PostLikeCount;
import cn.dbj.domain.rpc.repository.CounterRepository;
import cn.dbj.framework.starter.common.toolkit.Page;
import cn.dbj.types.dto.post.PostQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final IPostRepository postMongoRepository;
    private final CounterRepository<PostLikeCount> PostCounterRepository;


    @Override
    public Post queryPostDetail(String postId)
    {
        Post post = postMongoRepository.query(postId);
        //TODO 根据计数中心拼装点赞数据
        PostLikeCount count = PostCounterRepository.getCount(postId, post.getAuthorId());
        post.setLikeNum(count.getCount());
        //TODO 从评论中心加载评论

        return post;
    }


    @Override
    public Page queryUserPostList(PostQueryRequest queryRequest) {

        List<PostFeedBaseInfo> postFeedBaseInfos = postMongoRepository.queryUserPostList(queryRequest.getUid(), queryRequest.getPage(), queryRequest.getSize());
        postFeedBaseInfos.stream().map(postFeedBaseInfo -> {
            //TODO 根据计数中心拼装点赞数据
            PostLikeCount count = PostCounterRepository.getCount(postFeedBaseInfo.getId(), queryRequest.getUid());
            postFeedBaseInfo.setLikeNum(count.getCount());
            return postFeedBaseInfo;
        }).toList();

        Page<PostFeedBaseInfo> postFeedBaseInfoPage = new Page<>();
        postFeedBaseInfoPage.setContent(postFeedBaseInfos);
        postFeedBaseInfoPage.setPage(queryRequest.getPage());
        postFeedBaseInfoPage.setSize(queryRequest.getSize());

        return postFeedBaseInfoPage;
    }


    /**
     * 根据用户id查询所有点赞的帖子
     * @param userId
     * @return
     */
    @Override
    public boolean queryPostIdsByUserId(Long userId, String postId) {
        return postMongoRepository.isPostLikedByUser(userId,postId);
    }


    @Override
    public Map<String, Boolean> queryPostIdsByUserIdList(Long userId, List<String> postIds) {
        return postMongoRepository.isPostLikedByUserList(userId,postIds);
    }

    @Override
    public List<String> getPostListIds() {
        return postMongoRepository.getPostListIds();
    }

    @Override
    public List<String> queryPostListIdsByUserId(Long uid) {
        return postMongoRepository.getPostListByUserId(uid);
    }
}
