package cn.dbj.framework.starter.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "mydredis.database")
public class MydRedisProperties {

    private String host;
    private int port;
    private int database;
    private String password;

    // Getters and setters
    // 省略了 getters 和 setters 方法
}
