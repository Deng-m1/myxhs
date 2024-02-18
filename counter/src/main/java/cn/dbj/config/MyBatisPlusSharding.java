package cn.dbj.config;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.SneakyThrows;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;



public class MyBatisPlusSharding {

    private static final String STATEMENT_HANDLER_METHOD = "prepare";
    private static final String PARAMETERIZE_HANDLER_METHOD = "parameterize";

    @Autowired
    private YamlShardingRuleConfiguration yamlShardingRuleConfiguration;

    private Map<String, YamlTableRuleConfiguration> shardTableMap;

    @Bean
    public CustomInterceptor customInterceptor() {
        return new CustomInterceptor();
    }

    @Intercepts({
            @Signature(type = StatementHandler.class, method = STATEMENT_HANDLER_METHOD, args = {Connection.class, Integer.class}),
            @Signature(type = StatementHandler.class, method = PARAMETERIZE_HANDLER_METHOD, args = {Statement.class}),
    })
    public class CustomInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            Map<String, YamlTableRuleConfiguration> shardTableMap = getShardingTables();
            if (null == shardTableMap || shardTableMap.isEmpty()) {
                return invocation.proceed();
            }
            String method = invocation.getMethod().getName();
            switch (method) {
                case STATEMENT_HANDLER_METHOD:
                    return prepare(invocation);
                case PARAMETERIZE_HANDLER_METHOD:
                    return parameterize(invocation);
            }
            return invocation.proceed();
        }

        /**
         * 拦截Sql语法构建的处理
         * 处理sql的分表属性拼接
         */
        @SneakyThrows
        private Object prepare(Invocation invocation) {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            // 通过MetaObject优雅访问对象的属性，这里是访问statementHandler的属性;：MetaObject是Mybatis提供的一个用于方便、
            // 优雅访问对象属性的对象，通过它可以简化代码、不需要try/catch各种reflect异常，
            // 同时它支持对JavaBean、Collection、Map三种类型对象的操作。
            MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                    SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
            // 获取mapper的Statement对象,它描述的是mapper对象的配置
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            // 判断是否为更新方法
            if (SqlCommandType.UPDATE != mappedStatement.getSqlCommandType()) {
                return invocation.proceed();
            }
            // 获取数据库对象,此处需要@TableName注解获取表名并进行分表表名匹配
            Class pojoClazz = mappedStatement.getParameterMap().getType();
            if (!pojoClazz.isAnnotationPresent(TableName.class)) {
                return invocation.proceed();
            }
            TableName annotation = (TableName) pojoClazz.getAnnotation(TableName.class);
            if (!shardTableMap.containsKey(annotation.value())) {
                return invocation.proceed();
            }
            // 在sql尾部拼接分表所属属性
            YamlTableRuleConfiguration shardConfig = shardTableMap.get(annotation.value());
            String shardingColumn = shardConfig.getTableStrategy().getStandard().getShardingColumn(); // 分表属性
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            String sql = boundSql.getSql();
            sql = sql + " and " + shardingColumn + " = ?";
            metaObject.setValue("delegate.boundSql.sql", sql);
            return invocation.proceed();
        }

        /**
         * 拦截sql参数设置
         * 处理分表属性的设置
         */
        @SneakyThrows
        private Object parameterize(Invocation invocation) {
            RoutingStatementHandler routingStatementHandler = (RoutingStatementHandler) invocation.getTarget();
            ParameterHandler parameterHandler = routingStatementHandler.getParameterHandler();
            // 通过反射获取BoundSql,MappedStatement对象
            // TODO 优化获取BoundSql,MappedStatement对象, 反射不优雅
            BoundSql boundSql = routingStatementHandler.getBoundSql();
            Field mappedStatementField = parameterHandler.getClass().getDeclaredField("mappedStatement");
            mappedStatementField.setAccessible(true);
            MappedStatement mappedStatement = (MappedStatement) mappedStatementField.get(parameterHandler);
            // 判断是否为更新方法
            if (SqlCommandType.UPDATE != mappedStatement.getSqlCommandType()) {
                return invocation.proceed();
            }
            // 由于拦截器的本质是一个拦截器列表循环执行,所以需要重新获取相关数据
            Class pojoClazz = mappedStatement.getParameterMap().getType();
            if (!pojoClazz.isAnnotationPresent(TableName.class)) {
                return invocation.proceed();
            }
            TableName annotation = (TableName) pojoClazz.getAnnotation(TableName.class);
            if (!shardTableMap.containsKey(annotation.value())) {
                return invocation.proceed();
            }
            YamlTableRuleConfiguration shardConfig = shardTableMap.get(annotation.value());
            String shardingColumn = shardConfig.getTableStrategy().getStandard().getShardingColumn();
            // 数据库属性是下划线,代码是驼峰命名,所以需要对分表属性进行下划线转驼峰
            String camelCase = StrUtil.toCamelCase(shardingColumn);
            // 参数添加分表属性
            ParameterMapping parameterMapping = new ParameterMapping
                    .Builder(mappedStatement.getConfiguration(), "et." + camelCase, Object.class).build();
            boundSql.getParameterMappings().add(parameterMapping);
            return invocation.proceed();
        }
    }

    private Map<String, YamlTableRuleConfiguration> getShardingTables() {
        if (null != shardTableMap) {
            return shardTableMap;
        }
        synchronized (MyBatisPlusSharding.class) {
            if (null != shardTableMap) {
                return shardTableMap;
            }
            if (null == yamlShardingRuleConfiguration) {
                shardTableMap = Collections.emptyMap();
                return shardTableMap;
            }
            shardTableMap = yamlShardingRuleConfiguration.getTables();
            return shardTableMap;
        }
    }
}
