package cn.dbj.types.dto.post;


import cn.dbj.types.dto.base.RequestBody;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Posting请求体
 * @author daoqidelv
 * @createdate 2017年10月15日
 */
@Getter
@Setter
public class PostingReqBody extends RequestBody {

	@NotEmpty(message="{request.userId.not.empty}")
	private Long userId;

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	private String tid;

	private Boolean hotUser;
	
    private String title;

	private String topic;
    
    @NotEmpty(message="{request.post.posting.content.not.empty}")
    private String sourceContent;

	private String postType;
	/**
	 * 帖子首图，如果是视频类型就是视频
	 */
	private String mainShowUrl;
	/**
	 * 图片链接
	 */
	private List<String> url;



	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSourceContent() {
		return sourceContent;
	}

	public void setSourceContent(String sourceContent) {
		this.sourceContent = sourceContent;
	}  

}
