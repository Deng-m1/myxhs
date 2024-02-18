package cn.dbj.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountDTO implements Serializable {
    private Long uid;

    private String objId;


    private String countKey;

    private Long countValue;

    private Long lastCountValue;



    private List<String> keys;

    private Map<String, Integer> kv;


    public CountDTO(Long uid, String objId, String key, Long counterValue) {
        this.setUid(uid);
        this.setObjId(objId);
        this.setCountKey(key);
        this.setCountValue(counterValue);

    }
}


