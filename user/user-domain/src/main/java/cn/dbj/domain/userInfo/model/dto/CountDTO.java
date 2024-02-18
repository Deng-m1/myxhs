package cn.dbj.domain.userInfo.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CountDTO implements Serializable {
    private Long uid;

    private Long objId;

    private String objType;

    private String countKey;

    private Long countValue;

    private Long lastCountValue;



    private List<String> keys;

    private Map<String, Integer> kv;


}
