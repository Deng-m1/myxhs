package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import cn.dbj.infrastructure.entity.PostMongoDo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-27T20:53:53+0800",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
public class PostMongoTranslatorImpl implements PostMongoTranslator {

    @Override
    public PostMongoDo toPostMongo(Post post) {
        if ( post == null ) {
            return null;
        }

        List<String> topics = null;
        String tid = null;
        Long userId = null;
        String title = null;
        String sourceContent = null;
        Date postingTime = null;
        PostStatus status = null;
        PostType postType = null;
        String mainShowUrl = null;
        List<String> url = null;

        topics = mapSetToList( post.getTopics() );
        tid = post.getId();
        userId = post.getAuthorId();
        title = post.getTitle();
        sourceContent = post.getSourceContent();
        postingTime = post.getPostingTime();
        status = post.getStatus();
        postType = post.getPostType();
        mainShowUrl = post.getMainShowUrl();
        List<String> list1 = post.getUrl();
        if ( list1 != null ) {
            url = new ArrayList<String>( list1 );
        }

        PostMongoDo postMongoDo = new PostMongoDo( tid, title, userId, sourceContent, postingTime, status, topics, postType, mainShowUrl, url );

        return postMongoDo;
    }
}
