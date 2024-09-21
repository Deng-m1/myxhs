package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.entity.ElasticsearchPost;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.infrastructure.entity.PostMongoDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PostElasticsearchTranslator {
    PostElasticsearchTranslator INSTANCE = Mappers.getMapper(PostElasticsearchTranslator.class);

    ElasticsearchPost toPostFeedBaseInfoTranslator(PostMongoDo postMongoDo);

    PostMongoDo toPostMongoDo(ElasticsearchPost elasticsearchPost);


}