package cn.dbj.infrastructure.repository;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.domain.post.model.entity.TopicPost;
import cn.dbj.domain.post.repository.IPostRepository;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.mongodb.MongoBaseRepository;
import cn.dbj.infrastructure.dao.UserLikePostDao;
import cn.dbj.infrastructure.entity.PostMongoDo;
import cn.dbj.infrastructure.entity.UserLikePostDo;
import cn.dbj.infrastructure.translator.PostFeedBaseInfoTranslator;
import cn.dbj.infrastructure.translator.PostMongoTranslator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepository extends MongoBaseRepository<Post> implements IPostRepository {

    private final MongoTemplate mongoTemplate;

    private final DistributedCache distributedCache;


    private final UserLikePostDao userLikePostDao;
    /**
     * 判断用户是否给一个帖子点赞过
     * 抖音刷视频可以这么做，当然抖音也是一口气返回10个，20个视频，进行分页查询也不是一个一个查询
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public boolean isPostLikedByUser(Long userId, String postId) {
        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        // 查询ttl和minCid的值
        String ckey= RedisKeyConstant.USER_POST_LIKE + userId;
        if (!redisTemplate.hasKey(ckey)){
            //如果不存在，初始化
            initUserLikePostList(userId, redisTemplate, hashOperations, ckey, null);
        }else {
            if (hashOperations.hasKey(ckey, "ttl"))
            {
                Long ttl = (long)hashOperations.get(ckey, "ttl");
                initUserLikePostList(userId, redisTemplate, hashOperations, ckey, ttl);
            }
        }

        //查询minCid
        String minPostId = (String) hashOperations.get(ckey, "minPostId");


        String cachedPostId = (String) hashOperations.get(ckey, postId);
        if (cachedPostId!=null)
        {
            return true;
        }


        //如果大于minCid，并且在缓存中没有这一了
        if (Long.parseLong(postId)>Long.parseLong(minPostId) && cachedPostId==null){
            //说明没点赞过，直接返回
            return false;
        }

        //说明帖子是老数据需要查询数据库
        LambdaQueryWrapper<UserLikePostDo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserLikePostDo::getUserId, userId).eq(UserLikePostDo::getPostId, postId);
        boolean count = userLikePostDao.exists(lambdaQueryWrapper);
        if (!count){
            //说明没点赞过
            return false;
        }

        return true;
    }
    /**
     * 查询指定帖子信息
     *
     * @param postIds
     * @return Post
     */
    @Override
    public List<PostFeedBaseInfo> queryList(List<String> postIds) {
        Query query = new Query(Criteria.where("tid").in(postIds));
        List<PostMongoDo> postMongoDos = mongoTemplate.find(query, PostMongoDo.class);

        List<PostFeedBaseInfo> postFeedBaseInfos = new ArrayList<>();
        postMongoDos.forEach(postMongoDo -> {
            PostFeedBaseInfo postFeedBaseInfo = PostFeedBaseInfoTranslator.INSTANCE.toPostFeedBaseInfoTranslator(postMongoDo);
            postFeedBaseInfos.add(postFeedBaseInfo);
        });
        return postFeedBaseInfos;
    }


    /*
     * 判断是否给帖子点赞过列表
     * */
    @Override
    public Map<String, Boolean> isPostLikedByUserList(Long userId, List<String> postIds) {
        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        // 查询ttl和minCid的值
        String ckey= RedisKeyConstant.USER_POST_LIKE + userId;
        if (!redisTemplate.hasKey(ckey)){
            //如果不存在，初始化
            initUserLikePostList(userId, redisTemplate, hashOperations, ckey, null);
        }else {
            if (hashOperations.hasKey(ckey, "ttl"))
            {
                Long ttl = (long)hashOperations.get(ckey, "ttl");
                initUserLikePostList(userId, redisTemplate, hashOperations, ckey, ttl);
            }
        }


        final String mPostId = (String) hashOperations.get(ckey, "minPostId");
        //如果初始化后依然为null,直接返回为空
        if (mPostId==null){
            return postIds.stream().collect(Collectors.toMap(postId -> postId, postId -> false));
        }

        Map<String, Boolean> postIdMap = postIds.stream()
                .collect(Collectors.toMap(postId -> postId, postId -> false));
        Map<String, Boolean> missMap = new HashMap<>();
        List<Object> objects = hashOperations.multiGet(ckey, postIds);

        IntStream.range(0, objects.size())
                .mapToObj(index -> {
                    Object o = objects.get(index);
                    String curPostId= postIds.get(index);
                    if (o != null) {
                        // 在这里使用索引和对象 o 进行操作
                        postIdMap.put(curPostId, true);
                    }else {
                        if (Long.parseLong(curPostId)>Long.parseLong(mPostId)){
                            //说明没点赞过，直接返回
                            postIdMap.put(curPostId, false);
                        }else {
                            missMap.put(curPostId, false);
                        }
                    }
                    return o;
                });
        //对于feed流来说，一般都会命中
        if (missMap.size() > 0) {
            LambdaQueryWrapper<UserLikePostDo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserLikePostDo::getUserId, userId).in(UserLikePostDo::getPostId, missMap.keySet());
            List<UserLikePostDo> userLikePostDos = userLikePostDao.selectList(lambdaQueryWrapper);
            for (UserLikePostDo userLikePostDo : userLikePostDos) {
                missMap.put(userLikePostDo.getPostId(), true);
            }
        }

        postIdMap.putAll(missMap);
        return postIdMap;


    }

    /**
     * 查询指定帖子信息
     *
     * @param postId
     * @return Post
     */
    @Override
    public Post query(String postId) {
        Query query = new Query(Criteria.where("tid").is(postId));
        PostMongoDo postMongoDo = mongoTemplate.findOne(query, PostMongoDo.class);
        if (postMongoDo == null) {
            return null;
        }
        Post post = Post.builder()
                .authorId(postMongoDo.getUserId())
                .title(postMongoDo.getTitle())
                .sourceContent(postMongoDo.getSourceContent())
                .postingTime(postMongoDo.getPostingTime())
                .status(postMongoDo.getStatus())
                .postType(postMongoDo.getPostType())
                .mainShowUrl(postMongoDo.getMainShowUrl())
                .url(postMongoDo.getUrl())
                .topics(postMongoDo.getTopics().stream()
                        .map(topicId -> new TopicPost(topicId, postId))
                        .collect(Collectors.toSet()))
                .likeNum(0L) // 设置默认值，您可以根据需要进行修改
                .build();
        post.setId(postId);
        return post;
    }

    @Override
    public List<PostFeedBaseInfo> queryUserPostList(Long uid, int page, int size) {
        // 计算查询的起始索引
        int startIndex = page * size;

        // 创建查询条件
        Query query = new Query(Criteria.where("userId").is(uid));

        // 设置分页信息
        query.skip(startIndex); // 设置起始索引
        query.limit(size);       // 设置返回的文档数量

        // 执行查询操作，并将结果映射为指定类型的列表
        List<PostMongoDo> posts = mongoTemplate.find(query, PostMongoDo.class);

        // 将查询结果转换为所需的数据类型
        List<PostFeedBaseInfo> postFeedBaseInfos = new ArrayList<>();
        for (PostMongoDo post : posts) {
            // 假设你有一个方法可以将 UserPost 转换为 PostFeedBaseInfo，例如 toPostFeedBaseInfo 方法
            PostFeedBaseInfo postFeedBaseInfo = PostFeedBaseInfoTranslator.INSTANCE.toPostFeedBaseInfoTranslator(post);
            postFeedBaseInfos.add(postFeedBaseInfo);
        }

        return postFeedBaseInfos;
    }

    @Override
    public void likePost(Post post, Long uid) {
        String postId=post.getId();
        String ckey = RedisKeyConstant.USER_POST_LIKE + uid;

        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        if (!redisTemplate.hasKey(ckey))
        {
            //如果不存在，初始化
            initUserLikePostList(uid, redisTemplate, hashOperations, ckey, null);
        }

        //更新个人点赞信息表

        String minPostId=hashOperations.get(ckey,"minPostId").toString();
        if (Long.parseLong(postId)<Long.parseLong(minPostId))
        {
            minPostId=postId;
            hashOperations.put(RedisKeyConstant.USER_POST_LIKE,"minPostId",minPostId);
        }
        hashOperations.put(RedisKeyConstant.USER_POST_LIKE,postId,String.valueOf(System.currentTimeMillis()));
        hashOperations.put(RedisKeyConstant.USER_POST_LIKE,"ttl",String.valueOf(System.currentTimeMillis()+7*24*60*60*1000));

        //这一部可以放在
        UserLikePostDo userLikePostDo = new UserLikePostDo();
        userLikePostDo.setPostId(postId);
        userLikePostDo.setUserId(uid);
        userLikePostDao.insert(userLikePostDo);

    }

    public void save(Post post) {
        PostMongoDo postMongoDo = PostMongoTranslator.INSTANCE.toPostMongo(post);
        System.out.println(postMongoDo.getTid());
        System.out.println(postMongoDo.getUserId());

        // 使用 post.getId() 作为主键，直接存储帖子
        mongoTemplate.save(postMongoDo);

        super.save(post);
    }


    private void initUserLikePostList(Long userId, RedisTemplate redisTemplate, HashOperations<String, String, Object> hashOperations, String ckey, Long ttl) {
        String minPostId;
        if (ttl != null && ttl > 0 && System.currentTimeMillis() - ttl < 1 * 60 * 60 * 1000){
            // 更新时间戳
            hashOperations.put(ckey,
                    "ttl", System.currentTimeMillis()+ 7 * 24 * 60 * 60);
        }

        //对于ttl没有的话需要保存到缓存中一份。
        if (ttl ==null){
            LambdaQueryWrapper<UserLikePostDo> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            objectLambdaQueryWrapper.eq(UserLikePostDo::getUserId, userId).last("limit 400")
                    .orderBy(true, false, UserLikePostDo::getPostId);
            List<UserLikePostDo> userLikePostDos = userLikePostDao.selectList(objectLambdaQueryWrapper);// 替换为从数据库中查询帖子id的方法
            // 将查询到的数据存入缓存
            Map<String, Object> cacheData = new HashMap<>();
            for (UserLikePostDo postLike : userLikePostDos) {
                cacheData.put(postLike.getPostId(), postLike.getCreateTime());
            }
            if (userLikePostDos.size() == 0) {
                minPostId = "0";
            }else {
                minPostId = userLikePostDos.get(userLikePostDos.size()-1).getPostId();
            }
                //获取最小的帖子id(时间最早的帖子

            cacheData.put("minPostId" , String.valueOf(minPostId));
            long currentTime = System.currentTimeMillis();
            cacheData.put("ttl", String.valueOf(currentTime + 7 * 24 * 60 * 60));
            //存入缓存当中
            hashOperations.putAll(ckey, cacheData);
            // 设置过期时间（单位：秒）
            redisTemplate.expire(ckey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
        }
    }
}
