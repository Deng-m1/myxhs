package cn.dbj.infrastructure.repository;



import cn.dbj.framework.starter.common.domain.User;
import cn.dbj.infrastructure.entity.PostMongoDo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;


import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryFieldBuilders;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import org.springframework.data.elasticsearch.client.elc.QueryBuilders;
import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class PostElasticsearchRepository {

    private final ReactiveElasticsearchOperations operations;
    private final ElasticsearchTemplate elasticsearchTemplate;

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;
    public void test(){
        // 关键字查询，查询性为“女"的所有记录
        elasticsearchTemplate.search()
        TermQuery termQuery = QueryBuilders.termQuery("studentSex"/*字段名*/, "女"/*值*/);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();


        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
        SearchHits<Map> search = this.elasticsearchOperations.search(nativeSearchQuery, Map.class, IndexCoordinates.of("xx"/*索引名*/));
    }



    public Object singlePost(String word, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        Criteria criteria = new Criteria("price").is(42.0);
        Query query = new CriteriaQuery(criteria);

        Query query1 = new StringQuery("{ \"match\": { \"firstname\": { \"query\": \"Jack\" } } } ");
    }

    void createIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(User.class);
        //设置索引基本信息
        Map<String, Object> settings = new HashMap<>();
        Map<String, Object> index = new HashMap<>();
        settings.put("index", index);
        index.put("number_of_shards", 3);
        index.put("number_of_replicas", 1);

        Document mapping = indexOperations.createMapping(User.class);
        indexOperations.create(settings, mapping);
    }


}
