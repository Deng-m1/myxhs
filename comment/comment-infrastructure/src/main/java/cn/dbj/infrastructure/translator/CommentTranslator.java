package cn.dbj.infrastructure.translator;

import cn.dbj.domain.comment.model.aggregate.Comment;
import cn.dbj.infrastructure.entity.CommentDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CommentTranslator {
    CommentTranslator INSTANCE = Mappers.getMapper(CommentTranslator.class);
    @Mapping(source = "id",target = "id")
    CommentDo toCommentDo(Comment comment);

    List<Comment> toCommentList(List<CommentDo> commentDoList);

}

