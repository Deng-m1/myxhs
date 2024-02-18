package cn.dbj;

import com.jd.platform.hotkey.client.ClientStarter;
import jakarta.annotation.PostConstruct;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Configurable
@ComponentScan(value = {"cn.dbj.*"})
@MapperScan({"cn.dbj.infrastructure.*.dao","cn.dbj.mapper","cn.dbj.framework.starter.common.mysql"})
@EnableDubbo
public class UserApplication {

    public static void main(String[] args){
        SpringApplication.run(UserApplication.class);
    }

    @PostConstruct
    public void initHotkey() {

        ClientStarter.Builder builder = new ClientStarter.Builder();
        ClientStarter starter = builder.setAppName("user").setEtcdServer("http://127.0.0.1:12379,http://127.0.0.1:12379,http://127.0.0.1:12379").build();
        starter.startPipeline();
    }

}
