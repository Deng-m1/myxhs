package cn.dbj.framework.starter.common.mysql;

import cn.dbj.framework.starter.common.domain.AggregateRoot;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEventDao;
import cn.dbj.framework.starter.common.domain.event.publish.interception.ThreadLocalDomainEventIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.List.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public abstract class MysqlBaseRepository<AR extends AggregateRoot> {
    private final Map<String, Class> classMapper = new HashMap<>();

    @Autowired
    private DomainEventDao domainEventDao;

    @Transactional
    public void save(AR it) {
        requireNonNull(it, "AR must not be null.");

        if (!isEmpty(it.getEvents())) {
            saveEvents(it.getEvents());
            it.clearEvents();
        }
    }

    private void saveEvents(List<DomainEvent> events) {
        if (!isEmpty(events)) {
            domainEventDao.insert(events);
            ThreadLocalDomainEventIdHolder.addEvents(events);
        }
    }

    @Transactional
    public void delete(AR it) {
        requireNonNull(it, "AR must not be null.");

        if (!isEmpty(it.getEvents())) {
            saveEvents(it.getEvents());
            it.clearEvents();
        }
        /*mongoTemplate.remove(it);*/
    }

    @Transactional
    public void delete(List<AR> ars) {
        if (isEmpty(ars)) {
            return;
        }
        /*checkSameTenant(ars);*/

        List<DomainEvent> events = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        ars.forEach(ar -> {
            if (!isEmpty(ar.getEvents())) {
                events.addAll(ar.getEvents());
                ar.clearEvents();
            }
            ids.add(ar.getId());
        });

        saveEvents(events);
        /*mongoTemplate.remove(query(where("_id").in(ids)), getType());*/
    }

    /*@Transactional
    public void save(AR it) {
        requireNonNull(it, "AR must not be null.");

        if (!isEmpty(it.getEvents())) {
            saveEvents(it.getEvents());
            it.clearEvents();
        }

        mongoTemplate.save(it);
    }

    @Transactional
    public void save(List<AR> ars) {
        if (isEmpty(ars)) {
            return;
        }

        checkSameTenant(ars);
        List<DomainEvent> events = new ArrayList<>();
        ars.forEach(ar -> {
            if (!isEmpty(ar.getEvents())) {
                events.addAll(ar.getEvents());
                ar.clearEvents();
            }
            mongoTemplate.save(ar);
        });

        saveEvents(events);
    }

    @Transactional
    public void insert(AR it) {
        requireNonNull(it, "AR must not be null.");

        if (!isEmpty(it.getEvents())) {
            saveEvents(it.getEvents());
            it.clearEvents();
        }

        mongoTemplate.insert(it);
    }

    @Transactional
    public void insert(List<AR> ars) {
        if (isEmpty(ars)) {
            return;
        }

        checkSameTenant(ars);
        List<DomainEvent> events = new ArrayList<>();
        ars.forEach(ar -> {
            if (!isEmpty(ar.getEvents())) {
                events.addAll(ar.getEvents());
                ar.clearEvents();
            }
        });

        mongoTemplate.insertAll(ars);
        saveEvents(events);
    }

    @Transactional
    public void delete(AR it) {
        requireNonNull(it, "AR must not be null.");

        if (!isEmpty(it.getEvents())) {
            saveEvents(it.getEvents());
            it.clearEvents();
        }
        mongoTemplate.remove(it);
    }

    @Transactional
    public void delete(List<AR> ars) {
        if (isEmpty(ars)) {
            return;
        }
        checkSameTenant(ars);

        List<DomainEvent> events = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        ars.forEach(ar -> {
            if (!isEmpty(ar.getEvents())) {
                events.addAll(ar.getEvents());
                ar.clearEvents();
            }
            ids.add(ar.getId());
        });

        saveEvents(events);
        mongoTemplate.remove(query(where("_id").in(ids)), getType());
    }

    public AR byId(String id) {
        requireNonBlank(id, "AR ID must not be blank.");

        Object it = mongoTemplate.findById(id, getType());
        if (it == null) {
            throw new MyException(AR_NOT_FOUND, "未找到资源。",
                    mapOf("type", getType().getSimpleName(), "id", id));
        }

        return (AR) it;
    }

    public Optional<AR> byIdOptional(String id) {
        requireNonBlank(id, "AR ID must not be blank.");

        Object it = mongoTemplate.findById(id, getType());
        return it == null ? empty() : Optional.of((AR) it);
    }

    public AR byIdAndCheckTenantShip(String id, User user) {
        requireNonBlank(id, "AR ID must not be blank.");
        requireNonNull(user, "User must not be null.");

        AR ar = byId(id);
        checkTenantShip(ar, user);
        return ar;
    }

    public List<AR> byIds(Set<String> ids) {
        if (isEmpty(ids)) {
            return emptyList();
        }

        List<AR> ars = mongoTemplate.find(query(where("_id").in(ids)), getType());
        checkSameTenant(ars);
        return copyOf(ars);
    }

    public List<AR> byIdsAndCheckTenantShip(Set<String> ids, User user) {
        requireNonNull(user, "User must not be null.");

        if (isEmpty(ids)) {
            return emptyList();
        }

        List<AR> ars = byIds(ids);
        ars.forEach(ar -> checkTenantShip(ar, user));
        return copyOf(ars);
    }

    public List<AR> byIdsAll(Set<String> ids) {
        if (isEmpty(ids)) {
            return emptyList();
        }

        List<AR> ars = byIds(ids);
        if (ars.size() != ids.size()) {
            Set<String> fetchedIds = ars.stream().map(AggregateRoot::getId).collect(toImmutableSet());
            Set<String> originalIds = new HashSet<>(ids);
            originalIds.removeAll(fetchedIds);
            throw new MyException(AR_NOT_FOUND_ALL, "未找到所有资源。",
                    mapOf("type", getType().getSimpleName(), "missingIds", originalIds));
        }
        return copyOf(ars);
    }

    public List<AR> byIdsAllAndCheckTenantShip(Set<String> ids, User user) {
        requireNonNull(user, "User must not be null.");

        if (isEmpty(ids)) {
            return emptyList();
        }

        List<AR> ars = byIdsAll(ids);
        ars.forEach(ar -> checkTenantShip(ar, user));
        return copyOf(ars);
    }

    public int count(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        return (int) mongoTemplate.count(query, getType());
    }

    public boolean exists(String arId) {
        requireNonBlank(arId, "AR ID must not be blank.");
        Query query = query(where("_id").is(arId));
        return mongoTemplate.exists(query, getType());
    }

    private Class getType() {
        String className = getClass().getSimpleName();

        if (!classMapper.containsKey(className)) {
            Type genericSuperclass = getClass().getGenericSuperclass();
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            classMapper.put(className, (Class) actualTypeArguments[0]);
        }

        return classMapper.get(className);
    }

    protected final void checkTenantShip(AggregateRoot ar, User user) {
        requireNonNull(ar, "AR must not be null.");
        requireNonNull(user, "User must not be null.");

        if (!Objects.equals(ar.getTenantId(), user.getTenantId())) {
            throw new MyException(AR_NOT_FOUND, "未找到资源。", mapOf("id", ar.getId(), "tenantId", ar.getTenantId()));
        }
    }

    private void saveEvents(List<DomainEvent> events) {
        if (!isEmpty(events)) {
            domainEventDao.insert(events);
            ThreadLocalDomainEventIdHolder.addEvents(events);
        }
    }

    private void checkSameTenant(Collection<AR> ars) {
        Set<String> tenantIds = ars.stream().map(AR::getTenantId).collect(toImmutableSet());
        if (tenantIds.size() > 1) {
            Set<String> allArIds = ars.stream().map(AggregateRoot::getId).collect(toImmutableSet());
            throw new MyException(SYSTEM_ERROR, "All ars should belong to the same tenant.", "arIds", allArIds);
        }
    }*/
}
