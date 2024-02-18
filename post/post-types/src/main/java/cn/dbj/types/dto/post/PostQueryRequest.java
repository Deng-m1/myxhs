package cn.dbj.types.dto.post;

import cn.dbj.framework.starter.common.toolkit.QueryRequest;
import lombok.Data;


public class PostQueryRequest extends QueryRequest {
    Long uid;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
