package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.domain.post.model.entity.TopicPost;
import cn.dbj.infrastructure.entity.PostMongoDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PostFeedBaseInfoTranslator {
    PostFeedBaseInfoTranslator INSTANCE = Mappers.getMapper(PostFeedBaseInfoTranslator.class);
    @Mapping(source = "tid", target = "id")
    @Mapping(source = "userId", target = "authorId")
    PostFeedBaseInfo toPostFeedBaseInfoTranslator(PostMongoDo postMongoDo);


}
