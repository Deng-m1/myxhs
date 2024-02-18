/**
 * HTTP 接口服务
 */
package cn.dbj.trigger.http;


import cn.dbj.domain.userInfo.model.dto.FollowDTO;
import cn.dbj.domain.userInfo.model.dto.UserBaseDTO;
import cn.dbj.domain.userInfo.model.dto.UserDTO;

import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.convention.result.Result;
import cn.dbj.framework.starter.web.Results;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;

    @RequestMapping("/get/info")
    public Result<UserDTO> getUserInfo(@RequestParam Long id) {
        return Results.success(userService.getUserInfo(id));
    }

    @PostMapping("/set/follower")
    public Result<Boolean> FollowUser(@RequestBody FollowDTO followDTO){
        userService.followUser(followDTO);
        return Results.success(true);
    }

    @RequestMapping("/get/list")
    public Result<List<UserBaseDTO>> getUserList(@RequestParam Long id, @RequestParam String Type) {

        return Results.success(userService.getUserList(id,Type));
    }

    /*@RequestMapping("/get/followers")
    public Result<List<Integer>> getFollowers(Integer uid) {
        return userService.getFollowers(uid);
    }

    @RequestMapping("/get/attentions")
    public Result<List<Integer>> getAttentions(Integer uid) {
        return userService.getAttentions(uid);
    }

    @RequestMapping("/set/attention")
    public Result<List<Integer>> setAttention(@RequestBody Follower follower) {
        return userService.setFollowers(follower);
    }*/

}