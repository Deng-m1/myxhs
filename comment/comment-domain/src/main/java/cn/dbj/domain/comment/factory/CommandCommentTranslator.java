package cn.dbj.domain.comment.factory;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CommandCommentTranslator {
    CommandCommentTranslator INSTANCE = Mappers.getMapper(CommandCommentTranslator.class);




}
