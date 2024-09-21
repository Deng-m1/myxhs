package cn.dbj.infrastructure.repository;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.repository.ICommentRepository;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.mongodb.MongoBaseRepository;
import cn.dbj.infrastructure.entity.CommentDo;
import cn.dbj.infrastructure.translator.CommentTranslator;
import com.alibaba.fastjson2.JSON;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepository extends MongoBaseRepository<Comment> implements ICommentRepository {

    private final MongoTemplate mongoTemplate;

    private final DistributedCache distributedCache;

    private final RedissonClient redisson;


    public void save(Comment comment)
    {
        CommentDo commentDo = CommentTranslator.INSTANCE.toCommentDo(comment);
        mongoTemplate.save(commentDo);
        //TODO 更新缓存，可以采用队列，如果是热key才更新缓存
        super.save(comment);

    }
    @Override
    public List<Comment> queryCommentListByPostIdAndLastCommentId(String postId, int size, int page, String lastCommentId)
    {
        Query query;
        if (lastCommentId != null && !lastCommentId.isEmpty()) {
            // 如果传入了 lastCommentId，则使用它构建查询条件
            query = new Query(Criteria.where("postId").is(postId).and("_id").lt(lastCommentId));
        } else {
            // 否则，仅根据 postId 进行查询
            query = new Query(Criteria.where("postId").is(postId));
        }
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "_id")); // 按照 commentId 字段降序排序

        List<CommentDo> commentDoList = mongoTemplate.find(query, CommentDo.class);

        List<Comment> commentList = CommentTranslator.INSTANCE.toCommentList(commentDoList);
        return commentList;
    }
    @Override
    public List<Comment> queryCommentListWithoutCache(String postId, int size, int page, String lastCommentId)
    {
        return queryCommentListByPostIdAndLastCommentId(postId, size, page, lastCommentId);
    }

    @Override
    public List<Comment> queryCommentListWithCache(String postId, int size, int page, String lastCommentId)
    {
        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        String ckey=RedisKeyConstant.POST_COMMENT_FEED + postId;
        if(JdHotKeyStore.isHotKey(ckey))
        {
            if (size * page < 100)
            {
                if (!redisTemplate.hasKey(ckey))
                {
                    initPostCommentFeed(postId,redisTemplate);
                }
                List<Comment> commentList = getCommentsListPageFromCache(size, page, redisTemplate, ckey,lastCommentId);
                /*if (commentList != null) */return commentList;
            }
        }
        return queryCommentListByPostIdAndLastCommentId(postId, size, page, lastCommentId);
    }

    private List<Comment> getCommentsListPageFromCache(int size, int page, RedisTemplate redisTemplate, String ckey,String lastCommentId) {
        ListOperations<String,Object> listOperations = redisTemplate.opsForList();
        List<Object> range = listOperations.range(ckey, size * (page -1) , size * page);
        if(range!=null && range.size()>0)
        {
            List<CommentDo> commentDoList = range.stream().map(o -> JSON.parseObject(o.toString(), CommentDo.class))
                    .filter(commentDo -> Long.parseLong(commentDo.getId())<Long.parseLong(lastCommentId)).collect(Collectors.toList());
            List<Comment> commentList = CommentTranslator.INSTANCE.toCommentList(commentDoList);
            return commentList;
        }
        return null;
    }


    public List<Comment> queryCommentListByPostId(String postId,int size,int page)
    {
        Query query=new Query(Criteria.where("postId").is(postId));
        query.skip((long) page *size);
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "_id")); // 按照 createTime 字段降序排序
        List<CommentDo> all = mongoTemplate.query(CommentDo.class).matching(query).all();
        //TODO 查询缓存
        List<CommentDo> commentDoList = mongoTemplate.findAll(CommentDo.class);
        List<Comment> commentList = CommentTranslator.INSTANCE.toCommentList(commentDoList);
        return commentList;
    }

    @Override
    public void saveCommentToComment(Comment commentToComment) {
        save(commentToComment);
        /*CommentDo commentDo = CommentTranslator.INSTANCE.toCommentDo(commentToComment);

        pushCommentToCache(commentDo);*/
    }

    private void pushCommentToCache(CommentDo commentDo) {
        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        ListOperations<String,Object> listOperations = redisTemplate.opsForList();
        String key = RedisKeyConstant.POST_COMMENT_FEED + commentDo.getPostId();
        if (redisTemplate.hasKey(key))
        {

            listOperations.leftPush(key, JSON.toJSONString(commentDo));
            if (listOperations.size(key)>100)
            {
                listOperations.rightPop(key);
            }
        }else {
            initPostCommentFeed(commentDo.getPostId(),redisTemplate);
        }
    }

    private void initPostCommentFeed(String postId,RedisTemplate redisTemplate) {
        String key = RedisKeyConstant.POST_COMMENT_FEED + postId;
        if (!redisTemplate.hasKey(key))
        {
            RLock lock = redisson.getLock("Lock:" + key);
            try {
                // 尝试获取分布式锁，最多等待100秒，锁的持有时间为10秒
                if (lock.tryLock(100, 10, TimeUnit.SECONDS)) {
                    log.info("尝试获取锁");
                    if (!redisTemplate.hasKey(key))
                    {
                        List<CommentDo> comments = queryCommentListByPostIdFromDB(postId, 100, 0);
                        redisTemplate.opsForList().leftPushAll(key,comments.stream().map(JSON::toJSONString).collect(Collectors.toList()));
                    }
                }
            } catch (InterruptedException e) {
                // 获取锁的过程中被中断，可以执行相应的处理逻辑
                e.printStackTrace();
            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

    }

    public List<CommentDo> queryCommentListByPostIdFromDB(String postId,int size,int page)
    {
        Query query=new Query(Criteria.where("postId").is(postId).and("parentId").is("0"));
        query.skip((long) page *size);
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "createTime")); // 按照 createTime 字段降序排序
        List<CommentDo> all = mongoTemplate.query(CommentDo.class).matching(query).all();
        return all;
    }

    @Override
    public void saveCommentToPost(Comment commentToPost) {
        save(commentToPost);
        //TODO异步更新缓存
        /*CommentDo commentDo = CommentTranslator.INSTANCE.toCommentDo(commentToPost);

        pushCommentToCache(commentDo);*/
    }

    @Override
    public boolean exitsComment(String parentId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(parentId)); // 根据parentId查询是否存在评论

        return mongoTemplate.exists(query, CommentDo.class);
    }
}

