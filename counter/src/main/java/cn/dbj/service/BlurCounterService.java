package cn.dbj.service;


import cn.dbj.model.CountDTO;
import cn.dbj.model.Counter;

import java.util.List;
import java.util.Map;

/**
 * 计数接口用来实现计数的
 */
public interface BlurCounterService {

    /**
     * 获取单个计数
     * @param objId
     * @param key
     * @return
     */
    CountDTO getCounter(Long uid ,String objId, String key);

    /**
     * 写入单个计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    CountDTO setCounter(Long uid , String objId ,String key , Long value);

    /**
     * 获取多个计数
     * @param objIds
     * @param key
     * @return
     */

    List<CountDTO> getCounters(Long uid, String key, List<String> objIds);
    /**
     * 获取多个计数
     * @param uid
     * @param key
     * @return
     */

    List<CountDTO> getCounters(List<Long> uid, String key, String objId);

    /**
     * 批量写入数据
     * @param objId
     * @param objType
     * @param kv
     * @return
     */
    CountDTO setCounters(String objId , String objType , Map<String , Integer> kv);

    /**
     * 写入数据库
     * @param objId
     * @param key
     * @param value
     * @return
     */
    CountDTO setCounterDB(String objId , String key , Integer value);
}
