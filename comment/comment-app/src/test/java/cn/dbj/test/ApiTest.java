package cn.dbj.test;

import cn.dbj.domain.comment.service.CommentCommandService;
import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import cn.dbj.types.command.CreateCommentCommand;
import cn.dbj.types.command.CreateCommentResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Autowired

    @Test
    public void test() {
        log.info("测试完成");
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommentCommandService commentCommandService;


    @Test
    public void queryCommentListByPostIdAndLastCommentId() {
        List<String> postIds = getPostIds();

        String url = "http://localhost:8080/comment/post/queryPage?postId=1&size=10&page=1&lastCommentId=1";
        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(url, Result.class);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testCreateCommentssC() throws InterruptedException {
        // Prepare data
        List<String> postIds = getPostIds(); // Your method to get postIds
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        Random random = new Random();// Adjust the number of threads as per requirement
        Long num=0L;
        // Execute HTTP requests concurrently
        while (num<10000000000L){
            num++;
            Long index = (long) (random.nextInt(postIds.size()) + 1);
            String postId = postIds.get((int) (index % postIds.size()));
            executorService.execute(() -> {
                CreateCommentCommand commentCommand = prepareCommentCommand(postId);
                commentCommandService.createCommentToPost(commentCommand);
            });
        }
        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }


    @Test
    public void testCreateCommentsC() throws InterruptedException {
        // Prepare data
        List<String> postIds = getPostIdsByUserIds(); // Your method to get postIds
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        Random random = new Random();// Adjust the number of threads as per requirement
        int num=0;
        // Execute HTTP requests concurrently
        while (num<10000000){
            num++;
            Long index = (long) (random.nextInt(postIds.size()) + 1);
            String postId = postIds.get((int) (index % postIds.size()));
            executorService.execute(() -> {
                CreateCommentCommand commentCommand = prepareCommentCommand(postId);
                commentCommandService.createCommentToPost(commentCommand);
            });
        }
        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }

    @Test
    public void testCreateComments() throws InterruptedException {
        // Prepare data
        List<String> postIds = getPostIdsByUserIds(); // Your method to get postIds
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        Random random = new Random();// Adjust the number of threads as per requirement
        int num=0;
        // Execute HTTP requests concurrently
        while (num<10000000){
            num++;
            Long index = (long) (random.nextInt(postIds.size()) + 1);
            String postId = postIds.get((int) (index % postIds.size()));
            executorService.execute(() -> {
                CreateCommentCommand commentCommand = prepareCommentCommand(postId);
                ResponseEntity<CreateCommentResponse> responseEntity = restTemplate.postForEntity(
                        "http://localhost:8097/comment/creat/post",
                        commentCommand,
                        CreateCommentResponse.class);
                // Assert response status
                assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            });
        }

        // Shutdown executor service
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }

    private List<String> preparePostIds() {
        // Simulate getting postIds
        List<String> postIds = new ArrayList<>();
        // Add postIds to the list
        return postIds;
    }

    private CreateCommentCommand prepareCommentCommand(String postId) {
        // Prepare a random comment command
        CreateCommentCommand commentCommand = new CreateCommentCommand();
        Random random = new Random();
        Long userId = (long) (random.nextInt(100000) + 1); // 生成 1 到 1000 的随机数

        commentCommand.setUserId(userId); // Sample userId
        commentCommand.setPostId(postId);
        /*commentCommand.setPostId("1761469787222880256");*/
        int type = random.nextInt(5);

        if (type%4==0)commentCommand.setImageUrl(generateRandomUrl());
        /*commentCommand.setParentId(); //设置父评论Id
        commentCommand.setParentUserId();*/
        commentCommand.setTargetUserId(1L);
        commentCommand.setContent(generateRandomContentC()); // Sample comment content
        // Set other fields as per requirement

        return commentCommand;
    }

    public List<String> getPostIds() {
        String url = "http://localhost:8093/post/getPostList"; // 替换为实际的 API 地址
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(url, Result.class, headers);
        List<String> postIds = new ArrayList<>();
        Object data = responseEntity.getBody().getData();
        if (data instanceof List) {
            postIds = (List<String>) data; // 这里假设 data 已经是 List<String> 类型
        }
        ArrayList<String> result = new ArrayList<>();

        String regex = "\\{\"_id\": \"([^\"]+)\"\\}";

        Pattern pattern = Pattern.compile(regex);
        for (String postId : postIds) {
            Matcher matcher = pattern.matcher(postId);

            if (matcher.find()) {
                String idValue = matcher.group(1);
                result.add(idValue);
            }
        }
        return result;
    }


    public List<String> getPostIdsByUserIds() {
        String url = "http://localhost:8093/post/getPostListIdsByUserId?uid=1"; // 替换为实际的 API 地址
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(url, Result.class, headers);
        List<String> postIds = new ArrayList<>();
        Object data = responseEntity.getBody().getData();
        if (data instanceof List) {
            postIds = (List<String>) data; // 这里假设 data 已经是 List<String> 类型
        }
        ArrayList<String> result = new ArrayList<>();

        String regex = "\\{\"_id\": \"([^\"]+)\"\\}";

        Pattern pattern = Pattern.compile(regex);
        for (String postId : postIds) {
            Matcher matcher = pattern.matcher(postId);

            if (matcher.find()) {
                String idValue = matcher.group(1);
                result.add(idValue);
            }
        }
        return result;
    }


    // 生成随机中文内容的方法
    // 生成随机中英文混合内容的方法
    private String generateRandomContentC() {
        // 定义中文 Unicode 范围
        int minChinese = 0x4e00;
        int maxChinese = 0x9fff;

        // 随机数生成器
        Random random = new Random();

        // 生成随机中文内容
        StringBuilder stringBuilder = new StringBuilder();
        int count = random.nextInt(31) + 20; // 生成 20 到 50 个字符
        for (int i = 0; i < count; i++) {
            // 随机生成英文或中文
            boolean isChinese = random.nextBoolean();
            char randomChar;
            if (isChinese) {
                randomChar = (char) (minChinese + random.nextInt(maxChinese - minChinese + 1));
            } else {
                randomChar = (char) ('a' + random.nextInt(26));
            }
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
    // 生成随机URL链接
    private String generateRandomUrl() {
        // 这里简单返回一个固定的URL链接，你可以根据需要自行修改
        return "https://example.com/" + Math.random();
    }

    // 生成随机图片链接列表
    private List<String> generateRandomUrlList() {
        List<String> urlList = new ArrayList<>();
        // 生成随机数量的URL链接，这里仅生成三个URL链接作为示例
        for (int i = 0; i < 3; i++) {
            urlList.add(generateRandomUrl());
        }
        return urlList;
    }

    // 生成随机的 topic 字符串
    private String generateRandomTopic() {
        Random random = new Random();
        int count = random.nextInt(5); // 生成 0 到 4 个数字
        StringBuilder topicBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                topicBuilder.append("_"); // 添加下划线分隔符
            }
            int number = random.nextInt(20) + 1; // 生成 1 到 20 的随机数字
            topicBuilder.append(number); // 添加数字到 topic 字符串
        }
        return topicBuilder.toString();
    }

}
