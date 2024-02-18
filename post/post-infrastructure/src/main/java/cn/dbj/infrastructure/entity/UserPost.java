package cn.dbj.infrastructure.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("userPost")
@Setter
@Getter
public class UserPost {
    @Id
    @Indexed(unique = true)
    Long userId;
    List<PostMongoDo> postList;
}
