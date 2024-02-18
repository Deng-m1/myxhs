package cn.dbj.mapper;

import cn.dbj.model.Counter;
import cn.dbj.model.Do.CountDo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询counter数据库
 */
@Mapper
public interface CounterMapper extends BaseMapper<CountDo> {

   /* //单个查询数据库
    Integer getCounter(@Param("objId") Long objId, Param("key") String key);

    void setCounter(@Param("uid") Long uid, @Param("objId") Long objId, @Param("key") String key , @Param("value") Long value);

    //批量查询数据库
    List<CountDo> getCounters(@Param("objId") Long objId, @Param("keys") List<String> keys);

    List<CountDo> setCounters(@Param("list") List<Counter> list);*/



    void setCounter(@Param("uid") Long uid, @Param("objId") String objId, @Param("key") String key , @Param("value") Long value);


}
