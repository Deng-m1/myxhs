package cn.dbj.infrastructure.translator;

import cn.dbj.domain.rpc.count.PostLikeCount;
import cn.dbj.model.CountDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-18T02:40:49+0800",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
public class PostLikeTranslatorImpl implements PostLikeTranslator {

    @Override
    public PostLikeCount toPostLikeCount(CountDTO countDTO) {
        if ( countDTO == null ) {
            return null;
        }

        PostLikeCount postLikeCount = new PostLikeCount();

        postLikeCount.setUid( countDTO.getUid() );

        return postLikeCount;
    }
}
