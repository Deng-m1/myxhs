/**
 * 仓储实现；用于实现 domain 中定义的仓储接口，如；IXxxRepository 在 Repository 中调用服务
 */
package cn.dbj.infrastructure.user.repository;


import cn.dbj.domain.follow.repository.IAttentionRepository;
import cn.dbj.domain.follow.repository.IFollowRepository;
import cn.dbj.domain.userInfo.model.aggregate.User;
import cn.dbj.domain.userInfo.model.dto.UserBaseDTO;
import cn.dbj.domain.userInfo.repository.CounterRepository;
import cn.dbj.domain.userInfo.repository.IUserRepository;
import cn.dbj.framework.starter.cache.DistributedCache;
import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import cn.dbj.framework.starter.common.toolkit.BeanUtil;
import cn.dbj.infrastructure.user.dao.UserDao;
import cn.dbj.infrastructure.user.entity.UserDo;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.dbj.types.common.RedisKeyConstant.USER_INFO;


@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {
    private Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public final DistributedCache distributedCache;
    public final UserDao userDao;
    public final IFollowRepository followRepository;
    public final IAttentionRepository attentionRepository;
    public final KafkaTemplate kafkaTemplate;
    public final CounterRepository counterRepository;
    @Override
    public boolean existsById(Long id)
    {
        LambdaQueryWrapper<UserDo> eq = new LambdaQueryWrapper<>();
        eq.eq(UserDo::getId,id);
        return userDao.exists(eq);
    }



    public UserBaseDTO getUserInfo(Long id)
    {
        UserBaseDTO user=null;
        //是热key
        if (JdHotKeyStore.isHotKey(USER_INFO+id))
        {
            logger.info(USER_INFO+id+" is hotkey");
            user= (UserBaseDTO) JdHotKeyStore.get(USER_INFO+id);
            if(user == null) {
                user = getUserFromCache(id);
                JdHotKeyStore.smartSet(USER_INFO+id, user);
            }
            /*logger.info(userDo.getAttentionCount().toString());*/
        }else{
            logger.info(USER_INFO+id+" is not hotkey");
            user = getUserFromCache(id);
        }
        /*Optional.ofNullable(BeanUtil.convert(userDo,UserBaseDTO.class)).orElse(new UserBaseDTO());*/
        return user;
    }

    private UserBaseDTO getUserFromCache(Long id) {
        UserDo userDo;
        userDo= distributedCache.safeGet(
                USER_INFO+ id,
                UserDo.class,
                ()->userDao.selectById(id),
                1,
                TimeUnit.DAYS
        );
        UserBaseDTO userBaseDTO = convertToUserBase(userDo);
        Long followerCount= counterRepository.getCount(id, RedisKeyConstant.FOLLOWS_NUMBER);
        Long attentionCount = counterRepository.getCount(id, RedisKeyConstant.ATTENTIONS_NUMBER);
        userBaseDTO.setFollowerCount(followerCount);
        userBaseDTO.setAttentionCount(attentionCount);

        distributedCache.put(USER_INFO+ id,userBaseDTO);

        return userBaseDTO;
    }
    @Override
    public List<UserBaseDTO> getList(Long uid, String type)
    {
        //TODO 粉丝列表可能与关注列表展示的数据不同
        List<Long> userList = switch (type.toLowerCase()) {
            case "attention" -> attentionRepository.getAttentionList(uid);
            case "follow" ->
                    // Logic to get follow list, update as needed
                    followRepository.getFollowList(uid);
            default ->
                    // Handle unknown type or throw an exception based on your requirements
                    throw new IllegalArgumentException("Unknown type: " + type);
        };
        return getUserBaseList(userList);

    }

    @Override
    public void save(User user) {
        UserDo userDo = new UserDo();
        userDo.setId(user.getUid());
        userDo.setAge(user.getAge());
        userDo.setName(user.getName());
        userDo.setNickName(user.getNickName()); // 生成随机的昵称
        userDo.setDate(user.getDate());
        userDo.setDescription(user.getDescription());
        userDo.setImageUrl(user.getImageUrl());
        userDo.setSex(user.getSex());
        userDo.setStatus(user.getStatus().getStatusCode());

        userDo.setDelFlag(0L);


        userDao.insert(userDo);


        distributedCache.put(USER_INFO+ user.getId(),convertToUserBase(userDo));
    }

    private List<UserBaseDTO> getUserBaseList(List<Long> userList) {

        RedisTemplate instance = (RedisTemplate) distributedCache.getInstance();
        ValueOperations<String,String> valueOperations = instance.opsForValue();
        //从缓存中获取数据
        List<String> objects = valueOperations.multiGet(userList.stream().map(userId -> USER_INFO + userId)
                .collect(Collectors.toList()));

        //从缓存中获取数据
        Map<Long, UserBaseDTO> userBaseMap = (Objects.requireNonNull(objects))
                .stream()
                .filter(Objects::nonNull)
                .map(object->{
                    /*System.out.println(userDo);*/
                    return JSON.parseObject(object,UserBaseDTO.class);

                })
                .collect(Collectors.toMap(UserBaseDTO::getId, Function.identity(),(a, b)->a));
        System.out.println(userBaseMap.size());
        //找出没有命中的id
        List<Long> missingUserIds = userList.stream()
                .filter(userId -> !userBaseMap.containsKey(userId))
                .collect(Collectors.toList());

        if (!missingUserIds.isEmpty()) {
            Map<Long, UserBaseDTO> missingUserBaseMap = getUserBaseListFromDatabase(missingUserIds);
            userBaseMap.putAll(missingUserBaseMap);
        }

        return userList.stream()
                .map(userBaseMap::get)
                .collect(Collectors.toList());
    }

    private Map<Long, UserBaseDTO> getUserBaseListFromDatabase(List<Long> missingUserIds) {
        /*LambdaQueryWrapper<UserDo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(UserDo::getId,missingUserIds);*/


        List<UserDo> userDos = userDao.selectBatchIds(missingUserIds);
        Map<Long, UserBaseDTO> resultMap = userDos.stream().map(
                userDo -> {
                    UserBaseDTO userBaseDTO = convertToUserBase(userDo);
                    //三十分钟过期
                    distributedCache.put(USER_INFO+userBaseDTO.getId(), JSON.toJSONString(userBaseDTO),30,TimeUnit.SECONDS);
                    return userBaseDTO;
                }
        ).collect(Collectors.toMap(UserBaseDTO::getId, Function.identity()));

        return resultMap;
    }
    private UserBaseDTO convertToUserBase(UserDo userDo) {
        /*UserDo userDo = BeanUtil.convert(object, UserDo.class);*/
        /*String cacheKey = USER_INFO + userDo.getId();*/
        UserBaseDTO userBaseDTO = UserBaseDTO.builder()
                .id(userDo.getId())
                .age(userDo.getAge())
                .date(userDo.getDate())
                .name(userDo.getName())
                .sex(userDo.getSex())
                .status(userDo.getStatus())
                .nickName(userDo.getNickName())
                .attentionCount(0L)
                .followerCount(0L)
                .build();
        /*distributedCache.put(cacheKey, JSON.toJSONString(userDo));*/
        return userBaseDTO;
    }

}