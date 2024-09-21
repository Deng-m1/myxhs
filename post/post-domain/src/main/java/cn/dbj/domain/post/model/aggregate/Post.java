package cn.dbj.domain.post.model.aggregate; /**
 * 
 */


import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.dbj.domain.post.model.entity.Topic;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.events.PostPublishEvent;
import cn.dbj.domain.post.model.entity.TopicPost;
import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import cn.dbj.framework.starter.common.exception.ErrorCode;
import cn.dbj.framework.starter.common.exception.MyException;
import cn.dbj.types.common.CommonConstants;
import lombok.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static lombok.AccessLevel.PRIVATE;


/**
 * 帖子实体
 * @author DAOQIDELV
 * @CreateDate 2017年9月16日
 *
 */
@Getter
@NoArgsConstructor(access = PRIVATE)
@Setter
@Builder
@AllArgsConstructor
public class Post extends AggregateRoot {
	/**
     *帖子作者
     */  
    private long authorId;    
    /**
     * 帖子标题
     */
    private String title;
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
	private List<String> url;
    /**
     * 帖子加入的话题
     */
    private Set<TopicPost> topics = new HashSet<TopicPost>();

	public Long getLikeNum() {
		return likeNum;
	}

	public void setLikeNum(Long likeNum) {
		this.likeNum = likeNum;
	}

	/**
	 * 帖子点赞数
	 */
	private Long likeNum;
    
    private static int MAX_JOINED_TOPICS_NUM = 5;

    
    /*public Post(long id) {
    	this.setId(id);
    }*/
    
    public Post(long authorId, String title, String sourceContent) {
    	this();
    	this.setAuthorId(authorId);
    	this.setTitle(title);
    	this.setSourceContent(sourceContent);
    	/*this.setPostAuthor(new PostAuthor(authorId));*/
    }

	public Post(String tid,Long authorId) {
		super(tid);
		this.authorId=authorId;
		this.postingTime=new Date();
		/*this.postingTime = new Timestamp(System.currentTimeMillis());*/
		this.setStatus(PostStatus.WAIT_POSTED);
		this.setLikeNum(0L);
	}

	/**
     * 删除帖子
     */
    public void delete() {
    	this.setStatus(PostStatus.HAS_DELETED);
    }
    
    /**
     * 将帖子关联话题 
     * @param topicIds 话题集合
     */
    public void joinTopics(String topicIds) throws MyException {
    	if(StringUtils.isEmpty(topicIds)) {
    		return;
    	}
    	String[] topicIdArray = topicIds.split(CommonConstants.COMMA);
		for(int i=0; i<topicIdArray.length; i++) {
			TopicPost topicPost = new TopicPost(topicIdArray[i], this.getId());
    		this.topics.add(topicPost);
    		if(topicSize() > MAX_JOINED_TOPICS_NUM) {
    			throw new MyException(ErrorCode.EMPTY_FILLABLE_SETTING,"!");
    		}
		}
    }
    
    /**
     * 获取本帖子加入的话题数，参考jdk collection的api设计
     * @return 本帖子加入的话题数
     */
    public int topicSize() {
    	return this.topics.size();
    }

	public long getAuthorId() {
		return authorId;
	}
	/**
	 * @param authorId the authorId to set	 * 
	 * NOTE: avoid client only modify authorId, but not modify PostAuthor at the same time， set this setter private
	 */
	private void setAuthorId(long authorId) {
		Assert.isTrue(authorId > 0, "Post's authorId must greater than ZERO.");
		this.authorId = authorId;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the sourceContent
	 */
	public String getSourceContent() {
		return sourceContent;
	}
	/**
	 * @param sourceContent the sourceContent to set
	 */
	public void setSourceContent(String sourceContent) {
		//增加Assert断言，使实体满足一定的sepcification规格，参考Evans的《领域驱动设计》P154
		Assert.isTrue(!StringUtils.isEmpty(sourceContent), "Post's sourceContent must NOT be empty.");
		Assert.isTrue(sourceContent.length() >= 16, "Post's sourceContent at least have 16 words.");
		this.sourceContent = sourceContent;
	}
	/**
	 * @return the postingTime
	 */
	public Date getPostingTime() {
		return postingTime;
	}
	/**
	 * @param postingTime the postingTime to set
	 */
	public void setPostingTime(Timestamp postingTime) {
		this.postingTime = postingTime;
	}

	/**
	 * @return the topics
	 */
	public Set<TopicPost> getTopics() {
		return topics;
	}

	/**
	 * @param topics the topics to set
	 */
	public void setTopics(Set<TopicPost> topics) {
		this.topics = topics;
	}

	/**
	 * @return the status
	 */
	public PostStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(PostStatus status) {
		this.status = status;
	}

	public void publish(String title, String sourceContent, String topic,Boolean hotUser) {
		//TODO 为了方便先设置状态为已发布
		this.setStatus(PostStatus.HAS_POSTED);
		this.setTitle(title);
		this.setSourceContent(sourceContent);
		this.joinTopics(topic);
		//TODO 创建事件，1.如果是大v则，推送到发件箱 2.推送到话题 3.如果是大v则，推送到活跃粉丝 ，否则推送到所有粉丝
		this.raiseEvent(new PostPublishEvent(hotUser,this.getAuthorId(),this.getId(),this.getPostingTime()));

	}

    public void like(Long uid) {
		this.raiseEvent(new CountChangeEvent(uid,this.getId(), RedisKeyConstant.POST_LIKE,1L));
    }

	public void setId(String postId) {
		super.setId(postId);
	}


	public void publish(String title, String sourceContent, String topic, Boolean hotUser, String postType, String mainShowUrl, List<String> url) {
		//TODO 为了方便先设置状态为已发布
		this.setStatus(PostStatus.HAS_POSTED);
		this.setTitle(title);
		this.setSourceContent(sourceContent);
		this.joinTopics(topic);
		this.setType(postType);
		this.setMainShowUrl(mainShowUrl);
		this.setUrl(url);
		//TODO 创建事件，1.如果是大v则，推送到发件箱 2.推送到话题 3.如果是大v则，推送到活跃粉丝 ，否则推送到所有粉丝
		this.raiseEvent(new PostPublishEvent(hotUser,this.getAuthorId(),this.getId(),this.getPostingTime()));

	}

	private void setType(String postType) {
		if (postType.equals("00")) {
			this.setPostType(PostType.PICTURE);
		} else if (postType.equals("01")) {
			this.setPostType(PostType.VIDEO);
		}
	}

	/*public PostAuthor getPostAuthor() {
		return postAuthor;
	}

	*//**
	 * NOTE: avoid client only modify postAuthor, but not modify authorId at the same time, set this setter private
	 * @param postAuthor
	 *//*
	private void setPostAuthor(PostAuthor postAuthor) {
		this.postAuthor = postAuthor;
	}
	*/
}
