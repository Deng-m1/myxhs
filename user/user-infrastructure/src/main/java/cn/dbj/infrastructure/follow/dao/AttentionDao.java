/**
 * DAO 接口；IXxxDao
 */
package cn.dbj.infrastructure.follow.dao;


import cn.dbj.infrastructure.follow.entity.AttentionDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


public interface AttentionDao extends BaseMapper<AttentionDo> {
    public void insertOrUpdate(AttentionDo attentionDo);


}