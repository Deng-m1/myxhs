package cn.dbj.domain.feed.model.aggregate;

import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.framework.starter.common.domain.AggregateRoot;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class PostFeedReceiver extends AggregateRoot {

    private Long uid;

    private Date updateTime;

    private Set<String> postFollowerList;









}
