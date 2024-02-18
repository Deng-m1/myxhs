package cn.dbj.domain.post.fliter;

import cn.dbj.framework.starter.designpattern.chain.AbstractChainHandler;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.enums.PostChainMarkEnum;

public interface PostContextChainHandler<T extends PostingReqBody> extends AbstractChainHandler<PostingReqBody> {

    @Override
    default String mark() {
        return PostChainMarkEnum.LOCAL_TEXT_CONTENT.name();
    }
}
