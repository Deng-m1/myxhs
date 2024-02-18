package cn.dbj.framework.starter.common.scheduling;

import cn.dbj.framework.starter.common.domain.event.DomainEventJobs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static java.time.LocalDateTime.now;

@Slf4j
@Profile("!ci")
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "60m", defaultLockAtLeastFor = "10s")
public class SchedulingConfiguration {

    private final DomainEventJobs domainEventJobs;
    /*@Scheduled：支持cron格式，"0 0/2 * * * ?" 意味着“每2分钟”。

    @SchedulerLock，name参数必须是唯一的，ClassName_methodName通常足以实现它。我们不希望同时有多个相同名称方法运行，所以ShedLock使用唯一名称来实现该目的。

    llockAtLeastForString，以便我们可以在方法调用之间产生时间间隔。“PT3M”表示此方法至少可以锁定3分钟。这意味着这种方法可以由ShedLock运行，频率间隔不超过三分钟运行一次。也就是至少需要每4分钟运行一次

    lockAtMostForString用来指定在执行节点终止后应该保留多长时间。使用“PT14M”意味着它将被锁定不超过14分钟。

    在正常情况下，任务完成后，ShedLock会在任务完成后直接释放锁，这里，我们不需要这样做，因为在@EnableSchedulerLock中提供了一个默认值，但我们选择在这里重写它。*/

    //定时任务尽量放到前半个小时运行，以将后半个多小时留给部署时间

    //兜底发送尚未发送的事件，每2分钟运行，不能用@SchedulerLock，因为publishDomainEvents本身有分布式锁
    @Scheduled(cron = "0 */2 * * * ?")
    public void houseKeepPublishDomainEvent() {
        int count = domainEventJobs.publishDomainEvents();
        if (count > 0) {
            log.info("House keep published {} domain events.", count);
        }
    }



    @Bean
    public SchedulingTaskExecutor threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }
}
