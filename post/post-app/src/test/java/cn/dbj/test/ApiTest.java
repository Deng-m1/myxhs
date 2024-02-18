package cn.dbj.test;

import cn.dbj.domain.post.model.aggregate.Post;
import cn.dbj.domain.post.service.PostCommandService;
import cn.dbj.domain.post.service.PostQueryService;
import cn.dbj.framework.starter.common.toolkit.Page;
import cn.dbj.framework.starter.service.IQiniuService;
import cn.dbj.types.dto.post.PostQueryRequest;
import cn.dbj.types.dto.post.PostingReqBody;
import cn.dbj.types.dto.post.PostingRespBody;
import com.qiniu.common.QiniuException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.UUID;

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



}
