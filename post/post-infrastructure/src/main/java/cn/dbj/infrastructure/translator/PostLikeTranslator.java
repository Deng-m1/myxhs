package cn.dbj.infrastructure.translator;

import cn.dbj.domain.rpc.count.PostLikeCount;
import cn.dbj.model.CountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostLikeTranslator {
    PostLikeTranslator INSTANCE = Mappers.getMapper(PostLikeTranslator.class);
    PostLikeCount toPostLikeCount(CountDTO countDTO);



}
