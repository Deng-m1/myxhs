package cn.dbj.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.springframework.stereotype.Component;

/**
 * Custom Logback Appender for handling logging events in a Spring Boot application.
 * This appender processes log events and applies custom logic for events of ERROR level or higher.
 * 它可以做到针对系统中的任何层次、任务操作的异常都能捕获到Server端进行告警，而它里面就有一个基于logback的扩展点：
 * 自定义appender的设计实现，通过实现一个自定义的appender可以做到当程序中使用log.error或者log.info时，执行到自定义的appender代码
 * ，在代码中获得当前代码的信息，从而完成异常的上报。前阵子顺便研究了下只需要继承UnsynchronizedAppenderBase即可，代码示例如下：
 */
@Component
public class CustomerLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final static Level miniNumLeve=Level.ERROR;

    @Override
    public void start() {
        // 某些启动时候的参数校验

        // 存储起来配置文件
        super.start();
        addInfo("xxxxx init");
    }
    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        try {
            // 可以写写info级别的判断逻辑，在这里解析当前系统、方法、日志内容等


            // 如果当前日志对接级别大于等于error级别
            if (iLoggingEvent.getLevel().isGreaterOrEqual(miniNumLeve)){
                String message = iLoggingEvent.getMessage();
                // 未来这里的message进行规则判断

                // 判断是否包含异常
                final ThrowableProxy throwableProxy=(ThrowableProxy) iLoggingEvent.getThrowableProxy();
                if (throwableProxy!=null)
                {
                    Throwable throwable=throwableProxy.getThrowable();
                    // 解析异常类是否是可配置的
                }else {
                    // 未来这里可以判断消息是否需要告警
                }
            }else{

            }
        }catch (Exception e){
            addError("xxxx append error", e);
        }
    }
}
