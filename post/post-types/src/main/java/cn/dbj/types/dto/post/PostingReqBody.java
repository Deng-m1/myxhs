package cn.dbj.types.dto.post;


import cn.dbj.types.dto.base.RequestBody;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

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
