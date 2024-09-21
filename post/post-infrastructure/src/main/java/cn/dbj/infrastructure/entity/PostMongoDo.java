package cn.dbj.infrastructure.entity;

import cn.dbj.domain.post.model.entity.TopicPost;
import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
@Builder
@Data
@AllArgsConstructor
@Document("post")
public class PostMongoDo {

    @Id
    @Indexed(unique = true)
    private String tid;
    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子作者
     */

    private Long userId;
    /**
     * 帖子源内容
     */
    private String sourceContent;
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
    /**
     * 帖子作者
     */
    /*private PostAuthor postAuthor;*/
    /**
     * 帖子加入的话题
     */
    private List<String> topics;


     /**
     * 帖子类型
     */

    private PostType postType;
    /**
     * 帖子首图，如果是视频类型就是视频
     */
    private String mainShowUrl;
    /**
     * 图片链接
     */
    private List<String> url;
}
