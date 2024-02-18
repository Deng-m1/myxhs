package cn.dbj.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 计数实体类
 */
@Data
public class Counter implements Serializable {
    private Integer id;

    private Integer objId;

    private Integer objType;

    private String countKey;

    private Integer countValue;

    private Integer lastCountValue;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private List<String> keys;

    private Map<String, Integer> kv;

    public Counter() {
    }

    public Counter(Integer objId, String countKey, Integer countValue) {
        this.objId = objId;
        this.countKey = countKey;
        this.countValue = countValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getObjId() {
        return objId;
    }

    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public Integer getObjType() {
        return objType;
    }

    public void setObjType(Integer objType) {
        this.objType = objType;
    }

    public String getCountKey() {
        return countKey;
    }

    public void setCountKey(String countKey) {
        this.countKey = countKey;
    }

    public Integer getCountValue() {
        return countValue;
    }

    public void setCountValue(Integer countValue) {
        this.countValue = countValue;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getLastCountValue() {
        return lastCountValue;
    }

    public void setLastCountValue(Integer lastCountValue) {
        this.lastCountValue = lastCountValue;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Map<String, Integer> getKv() {
        return kv;
    }

    public void setKv(Map<String, Integer> kv) {
        this.kv = kv;
    }
}
