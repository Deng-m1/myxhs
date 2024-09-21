package cn.dbj.infrastructure.translator;

import cn.dbj.domain.post.model.entity.PostFeedBaseInfo;
import cn.dbj.infrastructure.entity.PostMongoDo;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-27T20:53:53+0800",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
public class PostFeedBaseInfoTranslatorImpl implements PostFeedBaseInfoTranslator {

    @Override
    public PostFeedBaseInfo toPostFeedBaseInfoTranslator(PostMongoDo postMongoDo) {
        if ( postMongoDo == null ) {
            return null;
        }

        PostFeedBaseInfo postFeedBaseInfo = new PostFeedBaseInfo();

        postFeedBaseInfo.setId( postMongoDo.getTid() );
        if ( postMongoDo.getUserId() != null ) {
            postFeedBaseInfo.setAuthorId( postMongoDo.getUserId() );
        }
        postFeedBaseInfo.setTitle( postMongoDo.getTitle() );
        postFeedBaseInfo.setPostingTime( postMongoDo.getPostingTime() );
        postFeedBaseInfo.setStatus( postMongoDo.getStatus() );
        postFeedBaseInfo.setPostType( postMongoDo.getPostType() );
        postFeedBaseInfo.setMainShowUrl( postMongoDo.getMainShowUrl() );

        return postFeedBaseInfo;
    }
}
