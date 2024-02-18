package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.TopicPost;
import cn.dbj.infrastructure.entity.PostMongoDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PostMongoTranslator {
    PostMongoTranslator INSTANCE = Mappers.getMapper(PostMongoTranslator.class);

    @Mapping(source = "topics", target = "topics")
    @Mapping(source = "id", target = "tid")
    @Mapping(source = "authorId", target = "userId")
    PostMongoDo toPostMongo(Post post);

    default List<String> mapSetToList(Set<TopicPost> topics) {
        // 将 Set 转换为 List
        // 在这里你可以根据实际需求进行转换逻辑的编写
        // 这里只是简单地将 Set 中的元素取出来放入 List 中
        return topics.stream()
                .map(TopicPost::getTopicId)
                .collect(Collectors.toList());
    }

    default Set<TopicPost> mapListToSet(List<String> topicIds,String id) {
        // 将 List 转换为 Set
        // 在这里你可以根据实际需求进行转换逻辑的编写
        // 这里只是简单地创建一个新的 HashSet，并将 List 中的元素放入其中
        return topicIds.stream()
                .map(topicId -> new TopicPost(topicId,id))
                .collect(Collectors.toSet());
    }
}
