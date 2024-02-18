package cn.dbj.infrastructure.follow.dao;

import cn.dbj.infrastructure.follow.entity.CountDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询counter数据库
 */

public interface CounterDao extends BaseMapper<CountDo> {

    //单个查询数据库
    Integer getCounter(@Param("objId") Long objId,@Param("objType") String objType, @Param("key") String key);

    void setCounter(@Param("objId") Long objId,@Param("objType") String objType, @Param("key") String key , @Param("value") Long value);

    //批量查询数据库
    List<CountDo> getCounters(@Param("objId") Long objId, @Param("objType") String objType, @Param("keys") List<String> keys);

    List<CountDo> setCounters(@Param("objType") Long objType,@Param("list") List<CountDo> list);

}
