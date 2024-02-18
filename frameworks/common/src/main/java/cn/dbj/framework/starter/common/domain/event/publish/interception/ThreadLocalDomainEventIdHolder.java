package cn.dbj.framework.starter.common.domain.event.publish.interception;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;

import java.util.LinkedList;
import java.util.List;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * 线程本地存储领域事件ID的工具类。
 */
public class ThreadLocalDomainEventIdHolder {

    /**
     * 线程本地存储的领域事件ID链表。
     */
    private static final ThreadLocal<LinkedList<String>> THREAD_LOCAL_EVENT_IDS = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 清空当前线程的领域事件ID链表。
     */
    public static void clear() {
        eventIds().clear();
    }

    /**
     * 移除当前线程的领域事件ID链表。
     */
    public static void remove() {
        THREAD_LOCAL_EVENT_IDS.remove();
    }

    /**
     * 获取当前线程的所有领域事件ID列表。
     *
     * @return 当前线程的所有领域事件ID列表
     */
    public static List<String> allEventIds() {
        List<String> eventIds = eventIds();
        return eventIds.isEmpty() ? List.of() : List.copyOf(eventIds);
    }

    /**
     * 将指定领域事件列表的ID添加到当前线程的领域事件ID链表中。
     *
     * @param events 领域事件列表
     */
    public static void addEvents(List<DomainEvent> events) {
        events.forEach(ThreadLocalDomainEventIdHolder::addEvent);
    }

    /**
     * 将指定领域事件的ID添加到当前线程的领域事件ID链表中。
     *
     * @param event 领域事件
     */
    public static void addEvent(DomainEvent event) {
        LinkedList<String> eventIds = eventIds();
        eventIds.add(event.getId());
    }

    /**
     * 获取当前线程的领域事件ID链表。
     *
     * @return 当前线程的领域事件ID链表
     */
    private static LinkedList<String> eventIds() {
        return THREAD_LOCAL_EVENT_IDS.get();
    }
}