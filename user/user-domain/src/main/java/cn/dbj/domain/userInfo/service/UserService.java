package cn.dbj.domain.userInfo.service;

import cn.dbj.domain.userInfo.model.dto.FollowDTO;
import cn.dbj.domain.userInfo.model.dto.UserBaseDTO;
import cn.dbj.domain.userInfo.model.dto.UserDTO;
import cn.dbj.types.command.CreatUserCommand;
import cn.dbj.types.command.CreatUserResponse;

import java.util.List;

public interface UserService {
    CreatUserResponse creatUser(CreatUserCommand creatUserCommand);

    CreatUserResponse creatUserTest(CreatUserCommand creatUserCommand);

    public UserDTO getUserInfo(long id);
    public void followUser(FollowDTO followDTO);

    public List<UserBaseDTO> getUserList(Long uid, String Type);
}