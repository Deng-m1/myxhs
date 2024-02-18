package cn.dbj;

import com.jd.platform.hotkey.client.ClientStarter;
import com.jd.platform.hotkey.client.Context;
import com.jd.platform.hotkey.client.core.rule.KeyRuleHolder;
import com.jd.platform.hotkey.client.etcd.EtcdConfigFactory;
import com.jd.platform.hotkey.common.configcenter.ConfigConstant;
import com.jd.platform.hotkey.common.configcenter.IConfigCenter;
import com.jd.platform.hotkey.common.rule.KeyRule;
import com.jd.platform.hotkey.common.tool.FastJsonUtils;
import jakarta.annotation.PostConstruct;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@SpringBootApplication
@Configurable
@EnableDubbo
@ComponentScan(value = {"cn.dbj.*"})
@MapperScan({"cn.dbj.infrastructure.dao","cn.dbj.mapper","cn.dbj.framework.starter.common.mysql"})
public class PostApplication {

    public static void main(String[] args){
        SpringApplication.run(PostApplication.class);
    }

    @PostConstruct
    public void initHotkey() {

        ClientStarter.Builder builder = new ClientStarter.Builder();
        ClientStarter starter = builder.setAppName("post").setEtcdServer("http://127.0.0.1:12379,http://127.0.0.1:12379,http://127.0.0.1:12379").build();
        starter.startPipeline();
        // 添加以下代码
        IConfigCenter configCenter = EtcdConfigFactory.configCenter();
        String rules = configCenter.get(ConfigConstant.rulePath + Context.APP_NAME);
        List<KeyRule> ruleList = FastJsonUtils.toList(rules, KeyRule.class);
        KeyRuleHolder.putRules(ruleList);
    }

}
