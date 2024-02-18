package cn.dbj.consumer;


import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventHandler;
import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.toolkit.MyTaskRunner;
import cn.dbj.model.CountDTO;
import cn.dbj.service.BlurCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountChangeEventHandler implements DomainEventHandler {
    public final BlurCounterService blurCounterService;
    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DomainEventType.CountChangeEvent;
    }

    @Override
    public void handle(DomainEvent domainEvent, MyTaskRunner taskRunner) {
        CountChangeEvent countChangeEvent= (CountChangeEvent) domainEvent;
        //set数据
        blurCounterService.setCounter(
                countChangeEvent.getUid(),
                countChangeEvent.getObjId(),
                countChangeEvent.getCountKey() , countChangeEvent.getCountValue());

    }
    /*private final SyncAttributeValuesForAllQrsUnderAppTask syncAttributeValuesForAllQrsUnderAppTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == ATTRIBUTES_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppAttributesCreatedEvent event = (AppAttributesCreatedEvent) domainEvent;
        Set<String> calculatedAttributeIds = emptyIfNull(event.getAttributes()).stream()
                .filter(it -> it.getAttributeType().isValueCalculated())
                .map(AttributeInfo::getAttributeId)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());

        if (isNotEmpty(calculatedAttributeIds)) {
            taskRunner.run(() -> syncAttributeValuesForAllQrsUnderAppTask.run(event.getAppId(), calculatedAttributeIds));
        }
    }*/
}

