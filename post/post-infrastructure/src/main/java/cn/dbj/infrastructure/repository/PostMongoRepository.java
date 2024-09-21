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
import cn.dbj.infrastructure.entity.UserPost;
import cn.dbj.infrastructure.translator.PostFeedBaseInfoTranslator;
import cn.dbj.infrastructure.translator.PostMongoTranslator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Slf4j
public class PostMongoRepository extends MongoBaseRepository<Post> implements IPostRepository {

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

    @Override
    public List<PostFeedBaseInfo> queryList(List<String> postIds) {
        return null;
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
            minPostId = userLikePostDos.get(userLikePostDos.size()-1).getPostId();
            cacheData.put("minPostId" , minPostId);
            long currentTime = System.currentTimeMillis();
            cacheData.put("ttl", currentTime + 7 * 24 * 60 * 60);
            //存入缓存当中
            hashOperations.putAll(ckey, cacheData);
            // 设置过期时间（单位：秒）
            redisTemplate.expire(ckey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
        }
    }

    /**
     * 查询指定帖子信息
     *
     * @param postId
     * @return Post
     */
    @Override
    public Post query(String postId) {
        Query query = new Query(Criteria.where("postList._id").is(postId));
        query.fields().elemMatch("postList", Criteria.where("_id").is(postId));
        /*PostMongoDo one = mongoTemplate.findOne(query, PostMongoDo.class);*/
        UserPost userPost = mongoTemplate.findOne(query, UserPost.class);
        PostMongoDo postMongoDo = userPost.getPostList().get(0);
        Post post = Post.builder()
                .authorId(userPost.getUserId())
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
    public List<String> getPostListByUserId(Long uid) {
        return null;
    }

    @Override
    public List<PostFeedBaseInfo> queryUserPostList(Long userId, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
          // 构建匹配条件
        MatchOperation matchUser = Aggregation.match(new Criteria("_id").is(userId));
        MatchOperation matchStatus = Aggregation.match(new Criteria("postList.status").is("HAS_POSTED"));
        // 构建分组操作
        GroupOperation groupOperation = Aggregation.group("_id")
                .push("postList").as("postList")
                .first("_class").as("_class"); // 保留原始 _class 信息;

          // 构建聚合管道

        Aggregation aggregation = Aggregation.newAggregation(
                matchUser,
                Aggregation.unwind("postList"),
                matchStatus,
                Aggregation.skip((long) pageRequest.getPageNumber() * pageRequest.getPageSize()),
                Aggregation.limit(pageRequest.getPageSize()),
                groupOperation
        );

            // 执行聚合查询
        AggregationResults<UserPost> aggregate = mongoTemplate.aggregate(aggregation, "userPost", UserPost.class);
        List<UserPost> mappedResults = aggregate.getMappedResults();
        List<PostMongoDo> postList = mappedResults.get(0).getPostList();
        List<PostFeedBaseInfo> collect = postList.stream().map(
                post -> {
                    PostFeedBaseInfo postFeedBaseInfo = PostFeedBaseInfoTranslator.INSTANCE.toPostFeedBaseInfoTranslator(post);
                    postFeedBaseInfo.setAuthorId(userId);
                    return postFeedBaseInfo;
                }
        ).collect(Collectors.toList());
        return collect;

    }
    /*@Override
    public List<PostFeedBaseInfo> queryPostList(List<String> postIds){
        Query query = new Query(Criteria.where("postList.tid").in(postIds));
        List<UserPost> userPosts = mongoTemplate.find(query, UserPost.class);

        List<PostFeedBaseInfo> collect = userPosts.stream().map(
                userPost -> {
                    List<PostMongoDo> postList = userPost.getPostList();
                    return postList.stream().map(
                            post -> {
                                PostFeedBaseInfo postFeedBaseInfo = PostFeedBaseInfoTranslator.INSTANCE.toPostFeedBaseInfoTranslator(post);
                                postFeedBaseInfo.setAuthorId(userPost.getUserId());
                                return postFeedBaseInfo;
                            }
                    ).collect(Collectors.toList());
                }
        ).flatMap(Collection::stream).collect(Collectors.toList());
        return collect;
    }*/

    @Override
    public void likePost(Post post, Long uid) {
        String postId=post.getId();
        RedisTemplate redistemplate = (RedisTemplate) distributedCache.getInstance();
        //更新个人点赞信息表
        String minPostId=redistemplate.opsForHash().get(RedisKeyConstant.USER_POST_LIKE,"minPostId").toString();
        if (Long.parseLong(postId)<Long.parseLong(minPostId))
        {
            minPostId=postId;
            redistemplate.opsForHash().put(RedisKeyConstant.USER_POST_LIKE,"minPostId",minPostId);
        }
        redistemplate.opsForHash().put(RedisKeyConstant.USER_POST_LIKE,postId,System.currentTimeMillis());
        redistemplate.opsForHash().put(RedisKeyConstant.USER_POST_LIKE,"ttl",System.currentTimeMillis()+7*24*60*60*1000);

        //这一部可以放在
        UserLikePostDo userLikePostDo = new UserLikePostDo();
        userLikePostDo.setPostId(postId);
        userLikePostDo.setUserId(uid);
        userLikePostDao.insert(userLikePostDo);
        userLikePostDao.insert(userLikePostDo);



    }

    @Override
    public List<String> getPostListIds() {
        Query query = new Query();
        query.fields().include("_id"); // Include only the _id field

        List<String> postIds = mongoTemplate.find(query, String.class, "post");

        return postIds;
    }

    /**
     * 保存指定帖子
     *
     * @param post
     */
    @Override
    public void save(Post post) {
        PostMongoDo postMongoDo = PostMongoTranslator.INSTANCE.toPostMongo(post);
        System.out.println(postMongoDo.getTid());

        UserPost userPost = new UserPost();
        userPost.setUserId(post.getAuthorId());
        userPost.setPostList(List.of(postMongoDo));
        Query query = new Query(Criteria.where("_id").is(post.getAuthorId()));
        // 创建更新操作，向指定用户的posts数组中添加新的帖子
        /*Update update = new Update().push("postList", postMongoDo);*/
        /*mongoTemplate.exists(query, UserPost.class);*/
        UpdateResult result=null;
        if (mongoTemplate.exists(query, UserPost.class)) {
            // 如果用户已存在，则直接添加新的帖子
            Query query1 = new Query(Criteria.where("_id").is(post.getAuthorId()).and("postList._id").is(post.getId()));
            if (mongoTemplate.exists(query1, UserPost.class))
            {
                Update update = new Update().set("postList.$", postMongoDo);
                result=mongoTemplate.updateFirst(query1, update, "userPost");
            }
            else {
                Update update = new Update().push("postList", postMongoDo);
                result=mongoTemplate.upsert(query, update, "userPost");
            }
        } else {
            // 如果用户不存在，则插入新的用户帖子列表
            mongoTemplate.insert(userPost);
        }
        /*UpdateResult result = mongoTemplate.upsert(query, update, "userPost");*/
        /*assert result != null;
        if (result.wasAcknowledged() && result.getModifiedCount() == 0 && result.getUpsertedId() != null) {
            // 如果没有更新现有文档，并且执行了插入操作，则打印日志或者处理其他逻辑
            System.out.println("Inserted a new document with _id: " + result.getUpsertedId());
        }*/
        /*mongoTemplate.insert(userPost);*/
        super.save(post);
    }
    /*@Override
    public void save(Post post) {
        PostMongoDo postMongoDo = PostMongoTranslator.INSTANCE.toPostMongo(post);

// 定义查询条件
        // 定义查询条件，查找指定用户的文档
    Query query = new Query(Criteria.where("_id").is(post.getAuthorId()));

    // 创建更新操作，向指定用户的 postList 数组中添加新的帖子
    Update update = new Update().addToSet("postList", postMongoDo).;

    // 执行更新操作
    UpdateResult result = mongoTemplate.upsert(query, update, UserPost.class);

// 检查更新结果
        if (result.getModifiedCount() == 0) {
            // 如果没有更新，表示用户不存在，插入新的用户帖子列表
            UserPost userPost = new UserPost();
            userPost.setUserId(post.getAuthorId());
            userPost.setPostList(List.of(postMongoDo));
            mongoTemplate.insert(userPost);
        }

        super.save(post);
    }*/
    /*PostMongoDo postDo = PostMongoDo.builder()
                .id(post.getId())
                .status(post.getStatus())
                .sourceContent(post.getSourceContent())
                .postingTime(post.getPostingTime())
                .title(post.getTitle())
                .topics(post.getTopics().stream().map(TopicPost::getTopicId).collect(Collectors.toList())).build();*/

    /**
     * 删除指定帖子
     *
     * @param post
     */
    @Override
    public void delete(Post post) {

    }



}
