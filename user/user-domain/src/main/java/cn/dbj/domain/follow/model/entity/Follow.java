/**
 * 值对象；
 * 1. 用于描述对象属性的值，如一个库表中有json后者一个字段多个属性信息的枚举对象
 * 2. 对象名称如；XxxVO
 */
package cn.dbj.domain.follow.model.entity;

import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.events.FollowCreatEvent;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
public class Follow extends AggregateRoot implements Serializable{
        private Long userId;
        private Long followerId;
        private Long isDelete;
        @JsonIgnore
        public static final String OBJ_ID = "1";
        public Follow(Long userId,Long followerId)
        {
                this.userId=userId;
                this.followerId=followerId;
                this.isDelete=0L;
                this.raiseEvent(new FollowCreatEvent(userId,followerId));
                this.raiseEvent(new CountChangeEvent(userId,OBJ_ID, RedisKeyConstant.FOLLOWS_NUMBER,1L));

        }
        public Follow(Long userId,Long followerId,Long isDelete)
        {
                this.userId=userId;
                this.followerId=followerId;
                this.isDelete=isDelete;
                //TODO 删除事件
                /*this.raiseEvent(new FollowCreatEvent(userId,followerId));*/
                this.raiseEvent(new CountChangeEvent(userId,OBJ_ID, RedisKeyConstant.FOLLOWS_NUMBER,-1L));
        }
}