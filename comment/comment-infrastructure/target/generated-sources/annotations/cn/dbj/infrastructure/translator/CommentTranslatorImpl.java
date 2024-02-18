package cn.dbj.infrastructure.translator;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.infrastructure.entity.CommentDo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-17T18:35:19+0800",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
public class CommentTranslatorImpl implements CommentTranslator {

    @Override
    public CommentDo toCommentDo(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDo commentDo = new CommentDo();

        commentDo.setId( comment.getId() );
        commentDo.setUserId( comment.getUserId() );
        commentDo.setPostId( comment.getPostId() );
        commentDo.setContent( comment.getContent() );
        commentDo.setParentId( comment.getParentId() );
        commentDo.setCommentType( comment.getCommentType() );
        commentDo.setParentUserId( comment.getParentUserId() );
        commentDo.setCreateTime( comment.getCreateTime() );
        commentDo.setDeleted( comment.getDeleted() );
        commentDo.setImageUrl( comment.getImageUrl() );

        return commentDo;
    }

    @Override
    public List<Comment> toCommentList(List<CommentDo> commentDoList) {
        if ( commentDoList == null ) {
            return null;
        }

        List<Comment> list = new ArrayList<Comment>( commentDoList.size() );
        for ( CommentDo commentDo : commentDoList ) {
            list.add( commentDoToComment( commentDo ) );
        }

        return list;
    }

    protected Comment commentDoToComment(CommentDo commentDo) {
        if ( commentDo == null ) {
            return null;
        }

        Comment comment = new Comment();

        comment.setUserId( commentDo.getUserId() );
        comment.setPostId( commentDo.getPostId() );
        comment.setContent( commentDo.getContent() );
        comment.setParentId( commentDo.getParentId() );
        comment.setParentUserId( commentDo.getParentUserId() );
        comment.setDeleted( commentDo.getDeleted() );
        comment.setCreateTime( commentDo.getCreateTime() );
        comment.setCommentType( commentDo.getCommentType() );
        comment.setImageUrl( commentDo.getImageUrl() );

        return comment;
    }
}
