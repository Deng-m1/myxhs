package cn.dbj.domain.follow.model.factory;

import cn.dbj.domain.follow.model.entity.Attention;
import cn.dbj.domain.follow.model.entity.Follow;
import cn.dbj.domain.follow.repository.IAttentionRepository;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.userInfo.repository.IUserRepository;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import cn.dbj.framework.starter.common.exception.ErrorCode;
import cn.dbj.framework.starter.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RelationFactory {
    public final IUserRepository userRepository;
    private final IFollowRepository followRepository;
    private final IAttentionRepository attentionRepository;
    public List<? extends AggregateRoot> creat(Long followerId, Long attAttentionId, Long isDelete)
    {
        if (attAttentionId!=null&&!userRepository.existsById(attAttentionId))
        {
            throw new MyException(ErrorCode.USER_NOT_CURRENT_MEMBER,"AttentionId所属用户不存在","AttentionId",attAttentionId);
        }
        if (followerId!=null&&!userRepository.existsById(followerId))
        {
            throw new MyException(ErrorCode.USER_NOT_CURRENT_MEMBER,"followerId所属用户不存在","followerId",followerId);
        }
        if (isDelete!=1&&followerId!=null&&attAttentionId!=null)
        {
            if (followRepository.existsRecord(attAttentionId,followerId)&&attentionRepository.existsRecord(followerId,attAttentionId))
            {
                throw new MyException(ErrorCode.DEPARTMENT_NAME_DUPLICATES,"不要重复关注","followerId",followerId,"AttentionId",attAttentionId);
            }
        }
        Follow follow = new Follow(attAttentionId, followerId);
        Attention attention=new Attention(followerId,attAttentionId);
        return Arrays.asList(follow,attention);
    }
    public List<? extends AggregateRoot> delete(Long followerId, Long attAttentionId, Long isDelete)
    {
        if (attAttentionId!=null&&!userRepository.existsById(attAttentionId))
        {
            throw new MyException(ErrorCode.USER_NOT_CURRENT_MEMBER,"AttentionId所属用户不存在","AttentionId",attAttentionId);
        }
        if (followerId!=null&&!userRepository.existsById(followerId))
        {
            throw new MyException(ErrorCode.USER_NOT_CURRENT_MEMBER,"followerId所属用户不存在","followerId",followerId);
        }
        /*if (isDelete!=1&&followerId!=null&&attAttentionId!=null)
        {
            if (followRepository.existsRecord(attAttentionId,followerId)&&attentionRepository.existsRecord(followerId,attAttentionId))
            {
                throw new MyException(ErrorCode.DEPARTMENT_NAME_DUPLICATES,"不要重复关注","followerId",followerId,"AttentionId",attAttentionId);
            }
        }*/
        Follow follow = new Follow(attAttentionId, followerId,isDelete);
        Attention attention=new Attention(followerId,attAttentionId,isDelete);
        return Arrays.asList(follow,attention);
    }

}
