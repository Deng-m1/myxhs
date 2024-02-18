package cn.dbj.framework.starter.common.mysql;

import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import cn.dbj.framework.starter.common.domain.constant.DomainEventStatus;

import static cn.dbj.framework.starter.common.domain.constant.DomainEventStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlDomainEventDao extends ServiceImpl<DomainEventMapper, DomainEvent> implements DomainEventDao {

    private final DomainEventMapper domainEventMapper;

    @Override
    public void insert(List<DomainEvent> events) {
        saveBatch(events);
    }

    @Override
    public DomainEvent byId(String id) {
        return getById(id);
    }

    @Override
    public List<DomainEvent> byIds(List<String> ids) {
        QueryWrapper<DomainEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        queryWrapper.orderByAsc("raised_at");
        return list(queryWrapper);
    }

    @Override
    public <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        QueryWrapper<DomainEvent> queryWrapper = new QueryWrapper<>();
        /*queryWrapper.eq("arId", arId);*/
        queryWrapper.eq("type", type);
        queryWrapper.orderByDesc("raised_at");
        return (T) getOne(queryWrapper);
    }

    @Override
    public boolean updateConsumed(DomainEvent event) {
        return false;
    }

    @Override
    public void successPublish(DomainEvent event) {
        updateStatusAndCount(event.getId(), PUBLISH_SUCCEED.name(), "published_count");
    }

    @Override
    public void failPublish(DomainEvent event) {
        updateStatusAndCount(event.getId(), PUBLISH_FAILED.name(), "published_count");
    }

    @Override
    public void successConsume(DomainEvent event) {
        updateStatusAndCount(event.getId(), CONSUME_SUCCEED.name(), "consumed_count");
    }

    @Override
    public void failConsume(DomainEvent event) {
        updateStatusAndCount(event.getId(), CONSUME_FAILED.name(), "consumed_count");

    }

    private void updateStatusAndCount(String id, String status, String countField) {
        UpdateWrapper<DomainEvent> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(DomainEvent::getId, id)
                .set(DomainEvent::getStatus, status)
                .setSql(countField + " = " + countField + " + 1");
        update(updateWrapper);
    }

    @Override
    public List<DomainEvent> tobePublishedEvents(String startId, int limit) {
        LambdaQueryWrapper<DomainEvent> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(DomainEvent::getStatus, CREATED, PUBLISH_FAILED, CONSUME_FAILED);
        lambdaQueryWrapper.gt(DomainEvent::getId, startId);
        lambdaQueryWrapper.lt(DomainEvent::getPublishedCount, 3);
        lambdaQueryWrapper.lt(DomainEvent::getConsumedCount, 3);
        lambdaQueryWrapper.orderByAsc(DomainEvent::getRaisedAt);
        lambdaQueryWrapper.last("LIMIT " + limit);
        DomainEventMapper baseMapper = this.getBaseMapper();

        List<DomainEvent> domainEventlist=baseMapper.selectList(lambdaQueryWrapper);
        System.out.println("dasdasdadasdads");
        return domainEventlist;
    }

}
