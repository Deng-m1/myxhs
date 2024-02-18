package cn.dbj.test;

import cn.dbj.domain.userInfo.model.dto.FollowDTO;
import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.common.domain.constant.DomainEventStatus;
import cn.dbj.framework.starter.common.events.AttentionCreatEvent;
import cn.dbj.framework.starter.common.events.CountChangeEvent;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import cn.dbj.types.command.CreatUserCommand;
import cn.dbj.types.command.CreatUserResponse;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Query.query;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Autowired
    public MongoTemplate mongoTemplate;

    @Autowired
    public UserService userService;

    /*@Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8091/user/set/follower";
        HttpHeaders headers = new HttpHeaders();
        FollowDTO followDTO = new FollowDTO();
        *//*MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("email", "844072586@qq.com");*//*
        for (int i=0;i<100;i++)
        {
            followDTO.setFollowerId(1L);
            followDTO.setAttentionId(24L);
            followDTO.setDelete(0L);
            HttpEntity<FollowDTO> request = new HttpEntity<>(followDTO, headers);
            ResponseEntity<FollowDTO> response = restTemplate.postForEntity( url, request , FollowDTO.class );
            System.out.println(response.getBody());
        }
        followDTO.setFollowerId(1L);
        followDTO.setAttentionId(24L);
        followDTO.setDelete(0L);
        HttpEntity<FollowDTO> request = new HttpEntity<>(followDTO, headers);
        ResponseEntity<FollowDTO> response = restTemplate.postForEntity( url, request , FollowDTO.class );
        System.out.println(response.getBody());
    }*/
    @Test
    public void test()
    {
        String startEventId = "EVT00000000000000001";
        Criteria criteria = new Criteria();
        criteria.and("status").is(DomainEventStatus.CREATED);
        Query query1 = new Query(criteria);
        Query query=query(Criteria.where("status").in("CREATED","PUBLISH_SUCCEED","CONSUME_FAILED"));
        /*Query query = query(Criteria.
                where("status").in("CREATED","PUBLISH_FAILED","CONSUME_FAILED")
                .and("_id").gt(startEventId)
                .and("publishedCount").lt(3)
                .and("consumedCount").lt(3))
                .with(by(ASC, "raisedAt"))
                .limit(100);*/
        List<AttentionCreatEvent> all = mongoTemplate.findAll(AttentionCreatEvent.class);
        List<DomainEvent> domainEvents1 = mongoTemplate.findAll(DomainEvent.class);
        List<DomainEvent> domainEvents = mongoTemplate.find(query,DomainEvent.class,"event");
        System.out.println(all.size());
        System.out.println(domainEvents1.size());
        System.out.println(domainEvents.size());
    }

    @Test
    public void test1()
    {
        String startEventId = "EVT00000000000000001";
        Query query = query(Criteria.
                where("status").in("CREATED","PUBLISH_FAILED","CONSUME_FAILED"));
                /*.and("_id").gt(startEventId));*/
                /*.and("publishedCount").lt(3)
                .and("consumedCount").lt(3))
                .with(by(ASC, "raisedAt"))
                .limit(100);*/
        /*List<AttentionCreatEvent> all = mongoTemplate.findAll(AttentionCreatEvent.class);*/
       /* List<DomainEvent> domainEvents1 = mongoTemplate.findAll((Class<DomainEvent>)DomainEvent.class);*/
        List<DomainEvent> domainEvents = mongoTemplate.find(query,DomainEvent.class);
        /*System.out.println(domainEvents1.size());*/
        System.out.println(domainEvents.size());
        for (DomainEvent d:domainEvents)
        {
            if (d.getType().name()=="COUNT_CHANGE")
            {
                CountChangeEvent countChangeEvent= (CountChangeEvent) d;
            }
        }
    }

    @Test
    public void testCreateMillionUsers() {
        Random random = new Random();
        for (long i = 1; i <= 1_000_000; i++) {
            CreatUserCommand command = CreatUserCommand.builder()
                    .id(i)
                    .name("User_" + i)
                    .imageUrl("url_" + i)
                    .sex(random.nextInt(2)) // 0: Female, 1: Male
                    .age(random.nextInt(100)) // Random age between 0 and 99
                    .birthday(generateRandomBirthday()) // Generate random birthday
                    .build();
            CreatUserResponse response = userService.creatUserTest(command);
            System.out.println("Created user with ID: " + response.getId());
        }
    }

    private Date generateRandomBirthday() {
        Random random = new Random();
        long minDay = LocalDate.of(1900, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2025, 1, 1).toEpochDay();
        long randomDay = minDay + random.nextInt((int) (maxDay - minDay));

        return new Date(randomDay);
    }

    @Test
    public void test2() {
        FollowDTO followDTO = new FollowDTO();
        followDTO.setFollowerId(24L);
        followDTO.setAttentionId(123L);
        followDTO.setDelete(0L);
        userService.followUser(followDTO);
    }
    @Autowired
    private RestTemplate restTemplate;

    public void followUser(FollowDTO followDTO) {
        String url = "http://localhost:8091/user/set/follower"; // 替换为实际的 API 地址
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FollowDTO> request = new HttpEntity<>(followDTO, headers);

        // 发起 POST 请求
        restTemplate.postForObject(url, request, Void.class);
    }

    @Test
    public void test3() {
        int threadCount = 100; // 线程数量
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<Pair<Long, Long>> generatedRelations = ConcurrentHashMap.newKeySet();
        int count = 100000000; // 创建一亿个关系
        AtomicInteger num=new AtomicInteger();

        // 创建多个任务
        Set<Callable<Void>> tasks = new HashSet<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (int j = 0; j < count; j++) {
                    /*Pair<Long, Long> relation = generateRandomRelation(generatedRelations, random);
                    Long followerId = relation.getKey();
                    Long attentionId = relation.getValue();*/
                    Long followerId = random.nextLong(1, 10); // 生成1到1000000之间的随机数
                    Long attentionId = random.nextLong(1, 1000001);
                    System.out.println(followerId+"----- "+attentionId);

                    FollowDTO followDTO = FollowDTO.builder()
                            .followerId(followerId)
                            .attentionId(attentionId)
                            .delete(0L)
                            .build();
                    followUser(followDTO);
                    System.out.println(num.incrementAndGet());

                    // 处理 followDTO 实例，可以将其存储到数据库或进行其他操作
                }
                return null;
            });
        }

        try {
            // 执行任务并等待所有任务完成
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭线程池
        executor.shutdown();
    }

    // 生成不重复的随机关系
    private static Pair<Long, Long> generateRandomRelation(Set<Pair<Long, Long>> generatedRelations, ThreadLocalRandom random) {
        long followerId;
        long attentionId;
        synchronized (generatedRelations) {
            do {
                followerId = random.nextLong(10, 50); // 默认大小不超过 1000000
                attentionId = random.nextLong(1, 1001);
            } while (generatedRelations.contains(Pair.of(followerId, attentionId)));
            generatedRelations.add(Pair.of(followerId, attentionId));
        }
        return Pair.of(followerId, attentionId);
    }
}
