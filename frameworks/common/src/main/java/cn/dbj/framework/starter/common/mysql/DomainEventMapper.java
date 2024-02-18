package cn.dbj.framework.starter.common.mysql;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DomainEventMapper extends BaseMapper<DomainEvent> {
}