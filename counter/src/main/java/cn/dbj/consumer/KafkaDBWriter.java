//package com.counter.consumer;
//
//import CounterMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Component
//public class KafkaDBWriter {
//
//    @Resource
//    private CounterMapper counterMapper;
//
//    private static final Logger logger = LoggerFactory.getLogger(KafkaDBWriter.class);
//
//    @KafkaListener(topics = "counter-topic")
//    public void consume(String message) {
//        String[] parts = message.split(":"); // 拆分成数组
//        String key = parts[0];
//        Integer objType = Integer.parseInt(parts[1]);
//        Integer objId = Integer.parseInt(parts[2]);
//        Integer value = Integer.parseInt(parts[3]);
//
//        try {
//            // 调用 mapper 更新数据库
//            counterMapper.setCounter(objId , objType , key, value);
//            logger.info("Write to database succeed, key: {}, value: {}", key, value);
//        } catch (Exception e) {
//            logger.error("Failed to write to database, key: {}, value: {}", key, value, e);
//            // 消费失败时可以根据具体情况进行重试或者发往错误处理队列等操作
//        }
//    }
//}
//
