package cn.dbj.framework.starter.common.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("my.redis")
public class MyRedisProperties {


    private String domainEventStream;


    private String notificationStream;


    private String webhookStream;

    private String use;

}