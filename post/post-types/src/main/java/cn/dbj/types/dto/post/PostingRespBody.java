package cn.dbj.types.dto.post;

import cn.dbj.types.dto.base.ResponseBody;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * Posting响应体
 * @author daoqidelv
 * @createdate 2017年10月15日
 */

@Setter
@Getter
public class PostingRespBody extends ResponseBody {

	private Long userId;


	private String title;

	private String topic;

	private String postId;

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}
}
