package cn.dbj.framework.starter.common.toolkit;

import cn.dbj.framework.starter.common.Constant.MyConstants;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.reflections.Reflections;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.time.Instant;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;
/*对象转换为stream*/
@Component
public class MyObjectMapper extends ObjectMapper {
    public MyObjectMapper() {
        configure(this);
    }

    private static void configure(ObjectMapper objectMapper) {
        // 获取标有 @TypeAlias("FOLLOW_CREAT_EVENT") 的类
        Reflections reflections = new Reflections("cn.dbj");
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(JsonTypeName.class);

        // 注册子类型
        classSet.forEach(aClass -> {
            TypeAlias typeAlias = aClass.getAnnotation(TypeAlias.class);
            if (typeAlias != null) {
                String aliasValue = typeAlias.value();
                objectMapper.registerSubtypes(new NamedType(aClass, aliasValue));
                System.out.println(new NamedType(aClass, aliasValue));
            }
        });
        /*Reflections reflections = new Reflections("cn.dbj");
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(JsonTypeName.class);
        classSet.parallelStream().forEach(aClass -> {objectMapper.setSubtypeResolver(aClass.getAnnotations())});*/
        classSet.parallelStream().forEach(ob-> System.out.println(ob.getName()));
        objectMapper.findAndRegisterModules()
                .setTimeZone(getTimeZone(of(MyConstants.CHINA_TIME_ZONE)))
                .setVisibility(ALL, NONE)
                .setVisibility(FIELD, ANY)
                .registerModule(instantModule())
                .registerModule(trimStringModule())
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static SimpleModule instantModule() {
        return new SimpleModule()
                .addSerializer(Instant.class, instantSerializer())
                .addDeserializer(Instant.class, instantDeserializer());
    }

    private static SimpleModule trimStringModule() {
        return new SimpleModule()
                .addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
                    @Override
                    public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                        return CommonUtils.nullIfBlank(jsonParser.getValueAsString().trim());
                    }
                });
    }

    private static JsonDeserializer<Instant> instantDeserializer() {
        return new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return Instant.ofEpochMilli(p.getValueAsLong());
            }
        };
    }

    private static JsonSerializer<Instant> instantSerializer() {
        return new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.toEpochMilli());
            }
        };
    }

    @Override
    public String writeValueAsString(Object value) {
        try {
            return super.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeValue(Writer w, Object value) {
        try {
            super.writeValue(_jsonFactory.createGenerator(w), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return super.readValue(content, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            return super.readValue(content, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> T readValue(InputStream src, Class<T> valueType) {
        try {
            return super.readValue(src, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonNode readTree(String content) {
        try {
            return super.readTree(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}