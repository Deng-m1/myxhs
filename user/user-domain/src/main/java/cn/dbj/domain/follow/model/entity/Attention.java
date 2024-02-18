/**
 * 值对象；
 * 1. 用于描述对象属性的值，如一个库表中有json后者一个字段多个属性信息的枚举对象
 * 2. 对象名称如；XxxVO
 */
package cn.dbj.domain.follow.model.entity;

import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.events.AttentionCreatEvent;
import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
public class Attention extends AggregateRoot implements Serializable {
    private Long userId;
    private Long attentionId;
    private Long isDelete;
    @JsonIgnore
    public static final Long OBJ_ID = 1L;
    public Attention(Long userId,Long attentionId)
    {
        this.userId=userId;
        this.attentionId=attentionId;
        this.isDelete=0L;
        this.raiseEvent(new AttentionCreatEvent(userId,attentionId));
        this.raiseEvent(new CountChangeEvent(OBJ_ID,userId.toString(), RedisKeyConstant.ATTENTIONS_NUMBER,1L));

    }
    public Attention(Long userId,Long attentionId,Long isDelete)
    {
        this.userId=userId;
        this.attentionId=attentionId;
        this.isDelete=isDelete;
        //TODO 删除事件
        /*this.raiseEvent(new FollowCreatEvent(userId,followerId));*/
        this.raiseEvent(new CountChangeEvent(OBJ_ID,userId.toString(), RedisKeyConstant.ATTENTIONS_NUMBER,-1L));
    }


}

