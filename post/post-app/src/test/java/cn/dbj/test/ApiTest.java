package cn.dbj.test;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.model.entity.ElasticsearchPost;
import cn.dbj.domain.post.repository.IPostElasticsearchRepository;
import cn.dbj.domain.post.service.PostCommandService;
import cn.dbj.domain.post.service.PostQueryService;
import cn.dbj.domain.userInfo.model.dto.FollowDTO;
import cn.dbj.framework.starter.common.toolkit.Page;
import cn.dbj.framework.starter.service.IQiniuService;
import cn.dbj.infrastructure.entity.PostMongoDo;
import cn.dbj.infrastructure.translator.PostElasticsearchTranslator;
import cn.dbj.types.dto.post.PostQueryRequest;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import com.qiniu.common.QiniuException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Autowired
    private PostCommandService postCommandService;

    @Autowired
    private PostQueryService postQueryService;
    @Test
    public void test() {
        log.info("测试完成");
    }

    @Autowired
    private IPostElasticsearchRepository iPostElasticsearchRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void save()
    {
        Query query=new Query();
        List<PostMongoDo> post = mongoTemplate.findAll(PostMongoDo.class, "post");
        List<ElasticsearchPost> collect = post.stream().map(PostElasticsearchTranslator.INSTANCE::toPostFeedBaseInfoTranslator).collect(Collectors.toList());
        collect.forEach(c->iPostElasticsearchRepository.save(c));


    }

    @Test
    public void test1() {
        PostingReqBody postingReqBody = new PostingReqBody();
        postingReqBody.setUserId(1L);

        PostingRespBody postingRespBody = postCommandService.creatPost(postingReqBody);
        System.out.println(postingRespBody.toString());

    }

    @Test
    public void test2() {
        String id="65c063a8beba0146e01f50f2";
        PostingReqBody postingReqBody = new PostingReqBody();
        postingReqBody.setUserId(1L);
        postingReqBody.setTid(id);
        postingReqBody.setSourceContent("Sdafaoihafioasidhaodhiasodasd");
        postingReqBody.setTitle("第一篇文章");

        postCommandService.publishPost(postingReqBody);
    }
    @Test
    public void test3() {
        String id="65c07b93350ebd73963ceb98)";
        PostingReqBody postingReqBody = new PostingReqBody();
        postingReqBody.setUserId(1L);
        postingReqBody.setTid(id);
        postingReqBody.setSourceContent("SdafaoihafioasidhaodhiasodasdNND");
        postingReqBody.setTitle("第一篇文章");

        postCommandService.publishPost(postingReqBody);
    }
    @Test
    public void test4() {
        Post post = postQueryService.queryPostDetail("65c063a8beba0146e01f50f2");
        System.out.println(post.toString());
    }

    @Autowired
    private IQiniuService qiniuService;

    @Test
    public void testUpload() throws QiniuException {
        String result = qiniuService.uploadFile(new File("C:\\Users\\12644\\Pictures\\IMG_20231101_1658271.jpg"), "helloworld");
        System.out.println("访问地址： " + result);
    }

    @Test
    public void testDelete() throws QiniuException {
        String result = qiniuService.delete("helloworld");
        System.out.println(result);
    }

    @Test
    public void testCreatAndPublish() {
        // 循环创建帖子并发布
        int numPostsToCreate = 10; // 设置要创建的帖子数量
        Long userId = 2L; // 设置创建帖子的用户 id
        Boolean hotUser= userId%2==0;
        for (int i = 0; i < numPostsToCreate; i++) {
            // 创建帖子并获取 tid
            PostingReqBody postingReqBody = new PostingReqBody();
            postingReqBody.setUserId(userId);
            PostingRespBody postingRespBody = postCommandService.creatPost(postingReqBody);
            String postId = postingRespBody.getPostId();
            System.out.println("Created post with tid: " + postId);

            // 发布帖子
            PostingReqBody newPostingReqBody = new PostingReqBody();
            newPostingReqBody.setUserId(userId);
            newPostingReqBody.setTid(postId); // 使用之前创建的 tid
            newPostingReqBody.setSourceContent("Some content " + generateRandomContent()); // 可以根据需要生成不同的内容
            newPostingReqBody.setTitle("Title " + i);
            newPostingReqBody.setHotUser(hotUser);

            postCommandService.publishPost(newPostingReqBody);
            System.out.println("Published post with title: " + newPostingReqBody.getTitle());
        }
    }

    @Test
    public void queryUserPostList() {
        // 循环创建帖子并发布
        int numPostsToCreate = 5; // 设置要创建的帖子数量
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setUid(1L);
        postQueryRequest.setPage(0);
        postQueryRequest.setSize(5);
        Page page = postQueryService.queryUserPostList(postQueryRequest);
        List content = page.getContent();
        for (Object o : content) {
            System.out.println(o);
        }
    }


    @Test
    public void likePost() {
        // 循环创建帖子并发布
        postCommandService.likePost("1758076955289264128", 1L);
    }

    private String generateRandomContent() {
        // 这里可以根据需要生成随机内容的逻辑
        return "Random content " + UUID.randomUUID().toString();
    }


    @Autowired
    private RestTemplate restTemplate;

    private String getRootUrl() {
        return "http://localhost:" + 8093 + "/post";
    }


    public void publishPost(PostingReqBody postingReqBody) {
        String url = "http://localhost:8093/post/publish"; // 替换为实际的 API 地址
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PostingReqBody> request = new HttpEntity<>(postingReqBody, headers);

        // 发起 POST 请求
        restTemplate.postForObject(url, request, Void.class);
    }
    public PostingRespBody createPost(PostingReqBody postingReqBody) {
        String url = "http://localhost:8093/post/create"; // 替换为实际的 API 地址
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PostingReqBody> request = new HttpEntity<>(postingReqBody, headers);

        // 发起 POST 请求
        PostingRespBody postingRespBody = restTemplate.postForObject(url, request, PostingRespBody.class);
        return postingRespBody;

    }
    @Test
    public void testCreatAndPublishHttp() {
        // 循环创建帖子并发布
/*        int numPostsToCreate = 10; // 设置要创建的帖子数量*/
        int totalPostsCreated = 0; // 记录已创建的帖子数量

        // 随机数生成器
        Random random = new Random();

        while (totalPostsCreated < 1000000 ) {
            // 随机生成 userId
            Long userId = (long) (random.nextInt(10000) + 1); // 生成 1 到 1000 的随机数

            // 根据 userId 判断用户是否为热门用户
            Boolean hotUser = userId % 2 == 0;

            // 随机生成帖子类型，0表示图文，1表示视频
            int postType = random.nextInt(2);
            String type="00";
            if (postType==0)
            {
                type="00";
            }else if (postType==1)
            {
                type="01";
            }
            String topics=generateRandomTopic();

            // 随机生成帖子内容
            String content = generateRandomContentC();

            // 随机生成帖子主图链接
            String mainShowUrl = generateRandomUrl();

            // 随机生成图片链接列表
            List<String> url = generateRandomUrlList();

            // 创建帖子并获取 postId
            PostingReqBody postingReqBody = new PostingReqBody();
            postingReqBody.setUserId(userId);
            PostingRespBody postingRespBody = createPost(postingReqBody);
            String postId = postingRespBody.getPostId();


            System.out.println("Created post with postId: " + postId);

            // 发布帖子
            PostingReqBody newPostingReqBody = new PostingReqBody();
            newPostingReqBody.setUserId(userId);
            newPostingReqBody.setTid(postId); // 使用之前创建的 postId

            newPostingReqBody.setSourceContent(content); // 设置帖子内容
            newPostingReqBody.setTitle("Title " + postId); // 设置帖子标题
            newPostingReqBody.setHotUser(hotUser); // 设置是否为热门用户
            newPostingReqBody.setTopic(topics);
            newPostingReqBody.setPostType(type);
            newPostingReqBody.setMainShowUrl(mainShowUrl);
            newPostingReqBody.setUrl(url);

            postCommandService.publishPost(newPostingReqBody);
            System.out.println("Published post with title: " + newPostingReqBody.getTitle());

            // 更新帖子数量统计
            totalPostsCreated++;
        }

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
