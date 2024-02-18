package cn.dbj.domain.feed.repository;

import java.util.List;

public interface PostFeedRepository {
    void creatPostFeedReceiver(Long uid,List<Long> attentionsLists);
}
