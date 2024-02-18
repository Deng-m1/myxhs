package cn.dbj.domain.comment.filter;

import cn.dbj.framework.starter.common.exception.ErrorCode;
import cn.dbj.framework.starter.common.exception.MyException;
import cn.dbj.types.command.CreateCommentCommand;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;
@Component
public class LocalTextContentChainHandler implements CommentContextChainHandler<CreateCommentCommand> {

    /**
     * 本地敏感词集合
     */
    private final Set<String> sensitiveWords = new HashSet<String>();

    public LocalTextContentChainHandler() {
        sensitiveWords.add("NND");
        sensitiveWords.add("奶奶个熊");
    }


    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 执行责任链逻辑
     *
     * @param requestParam 责任链执行入参
     */
    @Override
    public void handler(CreateCommentCommand requestParam) {
        Assert.isTrue(requestParam.getContent() != null, "LocalTextContentFilter filtContent's paramter must be String.");
        String content=requestParam.getContent();
        for(String sensitiveWord : sensitiveWords) {
            if(((String)content).contains(sensitiveWord)) {
                throw new MyException(ErrorCode.APP_ALREADY_LOCKED,"含有敏感词汇");
            }
        }

    }
}
