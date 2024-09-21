/**
 * 仓储服务
 * 1. 定义仓储接口，之后由基础设施层做具体实现
 */
package cn.dbj.domain.post.repository;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;

import java.util.List;
import java.util.Map;

/**
 * 帖子仓库接口
 * @author daoqidelv
 * @createdate 2017年10月15日
 */
public interface IPostRepository {
    boolean isPostLikedByUser(Long userId, String postId);

    List<PostFeedBaseInfo> queryList(List<String> postIds);

    Map<String, Boolean> isPostLikedByUserList(Long userId, List<String> postIds);

    /**
     * 查询指定帖子信息
     * @param postId
     * @return Post
     */
    Post query(String postId);

    List<String> getPostListByUserId(Long uid);

    /**
     * 保存指定帖子
     * @param post
     * @return int
     */
    void save(Post post);
    /**
     * 删除指定帖子
     * @param post
     * @return int
     */
    void delete(Post post);

    List<PostFeedBaseInfo> queryUserPostList(Long uid,int page,int size);

    void likePost(Post post, Long uid);

    List<String> getPostListIds();
}
