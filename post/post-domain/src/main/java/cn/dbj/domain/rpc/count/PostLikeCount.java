package cn.dbj.domain.rpc.count;

import lombok.Data;
import lombok.Getter;

@Data
public class PostLikeCount {
    String tid;
    Long uid;
    Long count;
}
