package cn.dbj.infrastructure.follow.entity;

import cn.dbj.framework.starter.database.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
* 关注者列表*/
@EqualsAndHashCode(callSuper = true)
@TableName("attention")
@Data
public class AttentionDo extends BaseDO {
    @TableId
    private Long id;
    private Long userId;
    private Long attentionId;

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

    public Long getAttentionId() {
        return attentionId;
    }

    public void setAttentionId(Long attentionId) {
        this.attentionId = attentionId;
    }
}
