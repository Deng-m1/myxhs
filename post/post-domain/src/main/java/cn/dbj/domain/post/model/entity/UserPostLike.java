package cn.dbj.domain.post.model.entity;

import lombok.Data;

@Data
public class UserPostLike {
    private Long uid;
    private String postId;
    private Long likeTime;
    private Boolean value;
}
