package cn.dbj.framework.starter.common.domain.event.publish.interception;

import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.framework.starter.common.domain.event.publish.DomainEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventHandlingInterceptor implements HandlerInterceptor {
    private final DomainEventPublisher eventPublisher;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {

        List<String> eventIds = ThreadLocalDomainEventIdHolder.allEventIds();
        /*List<DomainEvent> domainEvents = domainEventDao.byIds(eventIds);*/
        try {
            eventPublisher.publish(eventIds);
        } finally {
            ThreadLocalDomainEventIdHolder.remove();
        }
    }
}
