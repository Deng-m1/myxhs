package cn.dbj.framework.starter.common.configuration;

import org.bson.json.JsonWriterSettings;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static com.mongodb.ReadPreference.secondaryPreferred;
import static com.mongodb.WriteConcern.MAJORITY;
import static org.springframework.data.mongodb.core.WriteResultChecking.EXCEPTION;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> {
            builder.applyToConnectionPoolSettings(poolBuilder -> {
                poolBuilder.maxSize(500).minSize(5);
            });
        };
    }
    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
        mongoTemplate.setWriteConcern(MAJORITY);
        mongoTemplate.setWriteConcernResolver(action -> MAJORITY);
        mongoTemplate.setWriteResultChecking(EXCEPTION);
        mongoTemplate.setReadPreference(secondaryPreferred());
        return mongoTemplate;
    }

    /*@Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        //去掉_class字段
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));

        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory,mappingConverter);
        mongoTemplate.setWriteConcern(MAJORITY);
        mongoTemplate.setWriteConcernResolver(action -> MAJORITY);
        mongoTemplate.setWriteResultChecking(EXCEPTION);
        mongoTemplate.setReadPreference(secondaryPreferred());
        return mongoTemplate;
    }*/

    @Bean
    public JsonWriterSettings jsonWriterSettings() {
        return JsonWriterSettings.builder()
                .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
                .objectIdConverter((value, write) -> write.writeString(value.toString()))
                .dateTimeConverter((value, writer) -> writer.writeString(value.toString()))
                .build();
    }

}