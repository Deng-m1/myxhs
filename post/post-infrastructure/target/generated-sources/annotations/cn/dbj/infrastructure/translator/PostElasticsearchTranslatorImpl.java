package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.entity.ElasticsearchPost;
import cn.dbj.domain.post.model.valobj.PostStatus;
import cn.dbj.domain.post.model.valobj.PostType;
import cn.dbj.infrastructure.entity.PostMongoDo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-28T00:13:50+0800",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
public class PostElasticsearchTranslatorImpl implements PostElasticsearchTranslator {

    @Override
    public ElasticsearchPost toPostFeedBaseInfoTranslator(PostMongoDo postMongoDo) {
        if ( postMongoDo == null ) {
            return null;
        }

        ElasticsearchPost elasticsearchPost = new ElasticsearchPost();

        elasticsearchPost.setTid( postMongoDo.getTid() );
        elasticsearchPost.setTitle( postMongoDo.getTitle() );
        elasticsearchPost.setUserId( postMongoDo.getUserId() );
        elasticsearchPost.setSourceContent( postMongoDo.getSourceContent() );
        elasticsearchPost.setPostingTime( postMongoDo.getPostingTime() );
        elasticsearchPost.setStatus( postMongoDo.getStatus() );
        List<String> list = postMongoDo.getTopics();
        if ( list != null ) {
            elasticsearchPost.setTopics( new ArrayList<String>( list ) );
        }
        elasticsearchPost.setPostType( postMongoDo.getPostType() );
        elasticsearchPost.setMainShowUrl( postMongoDo.getMainShowUrl() );
        List<String> list1 = postMongoDo.getUrl();
        if ( list1 != null ) {
            elasticsearchPost.setUrl( new ArrayList<String>( list1 ) );
        }

        return elasticsearchPost;
    }

    @Override
    public PostMongoDo toPostMongoDo(ElasticsearchPost elasticsearchPost) {
        if ( elasticsearchPost == null ) {
            return null;
        }

        String tid = null;
        String title = null;
        Long userId = null;
        String sourceContent = null;
        Date postingTime = null;
        PostStatus status = null;
        List<String> topics = null;
        PostType postType = null;
        String mainShowUrl = null;
        List<String> url = null;

        tid = elasticsearchPost.getTid();
        title = elasticsearchPost.getTitle();
        userId = elasticsearchPost.getUserId();
        sourceContent = elasticsearchPost.getSourceContent();
        postingTime = elasticsearchPost.getPostingTime();
        status = elasticsearchPost.getStatus();
        List<String> list = elasticsearchPost.getTopics();
        if ( list != null ) {
            topics = new ArrayList<String>( list );
        }
        postType = elasticsearchPost.getPostType();
        mainShowUrl = elasticsearchPost.getMainShowUrl();
        List<String> list1 = elasticsearchPost.getUrl();
        if ( list1 != null ) {
            url = new ArrayList<String>( list1 );
        }

        PostMongoDo postMongoDo = new PostMongoDo( tid, title, userId, sourceContent, postingTime, status, topics, postType, mainShowUrl, url );

        return postMongoDo;
    }
}
