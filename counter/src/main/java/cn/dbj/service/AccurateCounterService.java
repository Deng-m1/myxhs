package cn.dbj.service;


import cn.dbj.model.CountDTO;
import cn.dbj.model.Counter;

/**
 * 精准计数实现
 */
public interface AccurateCounterService {

    /**
     * 获取计数
     * @param objId
     * @param key
     * @return
     */
    CountDTO getCounter(Long uid ,String objId, String key);

    /**
     * 写入计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    CountDTO setCounter(Long uid , String objId ,String key , Long value);

    /**
     * 删除计数
     * @param objId
     * @param key
     * @return
     */
    CountDTO delCounter(String objId, String objType  , String key);
}
