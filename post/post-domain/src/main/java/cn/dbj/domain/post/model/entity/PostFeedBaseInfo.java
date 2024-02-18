package cn.dbj.domain.post.model.entity;

import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class PostFeedBaseInfo {


    private String id;


    private long authorId;
    /**
     * 帖子标题
     */
    private String title;
    /**
     * 发帖时间
     */
    /*private Timestamp postingTime;*/
    private Date postingTime;
    /**
     * 帖子状态
     * NOTE：使用enum实现，限定status的字典值
     * @see PostStatus
     */
    private PostStatus status;
    /*
     * 帖子类型*/
    private PostType postType;
    /**
     * 帖子首图，如果是视频类型就是视频
     */
    private String mainShowUrl;
    /**
     * 图片链接
     */
    private Long likeNum;


    private Boolean userLike;

}
