package cn.dbj.domain.post.repository;

import cn.dbj.domain.post.model.entity.ElasticsearchPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface IPostElasticsearchRepository extends ElasticsearchRepository<ElasticsearchPost,String> {
    // 根据帖子标题进行全文检索




}
