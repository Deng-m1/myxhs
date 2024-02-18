package cn.dbj.infrastructure.entity;

import cn.dbj.framework.starter.database.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName("user_like_post")
@Data
public class UserLikePostDo extends BaseDO {

    @TableId
    private Long Id;

    private Long userId;

    private String postId;

}
