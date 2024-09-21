package cn.dbj.domain.post.model.entity;

import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Document(indexName = "posts")
@Data
public class ElasticsearchPost {

    @Id
    private String tid;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String sourceContent;

    @Field(type = FieldType.Date)
    private Date postingTime;

    @Field(type = FieldType.Keyword)
    private PostStatus status;

    @Field(type = FieldType.Keyword)
    private List<String> topics;

    @Field(type = FieldType.Keyword)
    private PostType postType;

    @Field(type = FieldType.Text)
    private String mainShowUrl;

    @Field(type = FieldType.Keyword)
    private List<String> url;




    // Getters and setters
}
