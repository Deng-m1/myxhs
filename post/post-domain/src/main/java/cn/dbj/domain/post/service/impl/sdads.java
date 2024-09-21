package cn.dbj.domain.post.service.impl;

import cn.dbj.domain.post.model.entity.ElasticsearchPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.QueryBuilders;
import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostElasticsearchRepositoryImpl implements PostElasticsearchRepositoryCustom {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public List<ElasticsearchPost> searchPosts(String keyword) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("sourceContent", keyword)) // 使用 sourceContent 进行模糊查询
                .withQuery(QueryBuilders.functionScoreQuery(QueryBuilders.matchQuery("sourceContent", keyword))
                        .add(QueryBuilders.fieldValueFactorFunction("likes").factor(0.3f)) // 点赞数作为权重，占比 30%
                        .add(QueryBuilders.decayFunction("postingTime", "1d", "7d", "30d").weight(0.7f))) // 时间衰减函数，时间越近权重越高，占比 70%
                .build();

        SearchHits<ElasticsearchPost> searchHits = elasticsearchRestTemplate.search(searchQuery, ElasticsearchPost.class);

        return searchHits.get().map(SearchHit::getContent).toList();
    }
}