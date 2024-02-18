/**
 * 持久化对象；XxxPO 最后的 PO 是大写，UserPO
 */
package cn.dbj.infrastructure.follow.entity;
/*
* userId的粉丝列表*/
import cn.dbj.framework.starter.database.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
/*
* 展示关注该用户的所有用户的信息
*/
import java.io.Serializable;
@TableName("follower")
public class FollowerDo extends BaseDO implements Serializable {
    /*
     * 被id
     */
    private Long id;
    /*
     * 被关注者id
     */
    private Long userId;
    /*
     * 关注者id
     */
    private Long followerId;


    // getter and setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

}