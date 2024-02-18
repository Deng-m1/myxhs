/**
 * 
 */
package cn.dbj.domain.post.model.entity;


/**
 * @author DAOQIDELV
 * @CreateDate 2017年9月16日
 *
 */
public class TopicPost {
	
	private String postId;
	
	private String topicId;
	
	public TopicPost(String topicId, String postId) {
		this.setPostId(postId);
		this.setTopicId(topicId);
	}

	/**
	 * @return the postId
	 */
	public String getPostId() {
		return postId;
	}

	/**
	 * @param postId the postId to set
	 */
	public void setPostId(String postId) {
		this.postId = postId;
	}

	/**
	 * @return the topicId
	 */
	public String getTopicId() {
		return topicId;
	}

	/**
	 * @param topicId the topicId to set
	 */
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
            return true;
        }
		if(anObject instanceof TopicPost) {
			if(this.postId == ((TopicPost)anObject).getPostId()
					&& this.topicId == ((TopicPost)anObject).getTopicId()) {
				return true;
			}
		} 
		return false;	
	}	
	
	/*@Override
    public int hashCode() {
    	return Long.hashCode(this.postId) ^ Long.hashCode(this.topicId);
    }*/

}
