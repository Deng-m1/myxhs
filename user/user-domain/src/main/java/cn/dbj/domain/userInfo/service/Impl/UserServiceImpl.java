package cn.dbj.domain.userInfo.service.Impl;

import cn.dbj.domain.follow.model.entity.Attention;
import cn.dbj.domain.follow.model.entity.Follow;
import cn.dbj.domain.follow.model.factory.RelationFactory;
import cn.dbj.domain.follow.repository.IAttentionRepository;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.userInfo.model.aggregate.User;
import cn.dbj.domain.userInfo.model.dto.FollowDTO;
import cn.dbj.domain.userInfo.model.dto.UserBaseDTO;
import cn.dbj.domain.userInfo.model.dto.UserDTO;
import cn.dbj.domain.userInfo.model.factory.UserFactory;
import cn.dbj.domain.userInfo.repository.CounterRepository;
import cn.dbj.domain.userInfo.repository.IUserRepository;
import cn.dbj.domain.userInfo.service.UserService;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import cn.dbj.framework.starter.common.toolkit.BeanUtil;
import cn.dbj.types.command.CreatUserCommand;
import cn.dbj.types.command.CreatUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public final IUserRepository userRepository;

    private final IFollowRepository followRepository;

    private final IAttentionRepository attentionRepository;
    private final RelationFactory relationFactory;
    private final CounterRepository counterRepository;

    @Override
    public CreatUserResponse creatUser(CreatUserCommand creatUserCommand) {
        User user = UserFactory.creatUser(creatUserCommand.getName(),creatUserCommand.getImageUrl(),creatUserCommand.getBirthday(),creatUserCommand.getSex(),creatUserCommand.getAge());
        userRepository.save(user);
        return CreatUserResponse.builder().id(user.getUid()).build();
    }
    @Override
    public CreatUserResponse creatUserTest(CreatUserCommand creatUserCommand) {
        User user = UserFactory.creatUserTest(creatUserCommand.getId(),creatUserCommand.getName(),creatUserCommand.getImageUrl(),creatUserCommand.getBirthday(),creatUserCommand.getSex(),creatUserCommand.getAge());
        userRepository.save(user);
        return CreatUserResponse.builder().id(user.getUid()).build();
    }

    public UserDTO getUserInfo(long id)
    {
        return BeanUtil.convert(userRepository.getUserInfo(id),UserDTO.class);
    }

    /*
    * 关注*/
    @Override
    public void followUser(FollowDTO followDTO) {
        //1 关注 2号 则 1 是 2 的粉丝      2是follow里的userid 1是followid
        // 1是attention的userId 2是attentionId
        if (followDTO.getDelete()==0)
        {
            List<? extends AggregateRoot> creat = relationFactory.creat(followDTO.getFollowerId(), followDTO.getAttentionId(), followDTO.getDelete());
            attentionRepository.save((Attention) creat.get(1));

            followRepository.save((Follow) creat.get(0));
        }else {
            List<? extends AggregateRoot> delete = relationFactory.delete(followDTO.getFollowerId(), followDTO.getAttentionId(), followDTO.getDelete());
            attentionRepository.save((Attention) delete.get(1));
            followRepository.save((Follow) delete.get(0));
        }

    }
    @Override
    public List<UserBaseDTO> getUserList(Long uid, String Type)
    {
        List<UserBaseDTO> list = userRepository.getList(uid, Type);

        List<Long> collect = list.stream().map(UserBaseDTO::getId).collect(Collectors.toList());

        //获取点赞数
        Map<Long, Long> countFollowList = counterRepository.getCountList(collect, RedisKeyConstant.FOLLOWS_NUMBER);
        Map<Long, Long> countAttentionList1 = counterRepository.getCountList(collect, RedisKeyConstant.ATTENTIONS_NUMBER);


        list.forEach(userBaseDTO -> {
            userBaseDTO.setAttentionCount(countAttentionList1.get(userBaseDTO.getId()));
            userBaseDTO.setFollowerCount(countFollowList.get(userBaseDTO.getId()));
        });

        return list;
    }

}
