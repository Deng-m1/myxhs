package cn.dbj.infrastructure.repository;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.domain.comment.repository.ICommentRepository;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.mongodb.MongoBaseRepository;
import cn.dbj.infrastructure.entity.CommentDo;
import cn.dbj.infrastructure.translator.CommentTranslator;
import com.alibaba.fastjson2.JSON;
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
import java.util.Optional;
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

    public List<Comment> queryCommentListByPostId(String postId,int size,int page)
    {
        Query query=new Query(Criteria.where("postId").is(postId));
        query.skip((long) page *size);
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "createTime")); // 按照 createTime 字段降序排序
        List<CommentDo> all = mongoTemplate.query(CommentDo.class).matching(query).all();
        //TODO 查询缓存
        List<CommentDo> commentDoList = mongoTemplate.findAll(CommentDo.class);
        List<Comment> commentList = CommentTranslator.INSTANCE.toCommentList(commentDoList);
        return commentList;
    }

    @Override
    public void saveCommentToComment(Comment commentToComment) {
        save(commentToComment);

        pushCommentToCache(commentToComment);
    }

    private void pushCommentToCache(Comment commentToComment) {
        RedisTemplate redisTemplate = (RedisTemplate) distributedCache.getInstance();
        ListOperations<String,Object> listOperations = redisTemplate.opsForList();
        String key = RedisKeyConstant.POST_COMMENT_FEED + commentToComment.getPostId();
        if (redisTemplate.hasKey(key))
        {

            listOperations.leftPush(key, commentToComment);
            if (listOperations.size(key)>100)
            {
                listOperations.rightPop(key);
            }
        }else {
            initPostCommentFeed(commentToComment.getPostId(),redisTemplate);
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
                        redisTemplate.opsForList().leftPushAll(key,comments.stream().map(c->{
                            return JSON.toJSONString(c);
                        }).collect(Collectors.toList()));
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
    public void saveCommentToPost(Comment commentToComment) {
        save(commentToComment);
        pushCommentToCache(commentToComment);
    }

    @Override
    public boolean exitsComment(String parentId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(parentId)); // 根据parentId查询是否存在评论

        return mongoTemplate.exists(query, CommentDo.class);
    }
}

