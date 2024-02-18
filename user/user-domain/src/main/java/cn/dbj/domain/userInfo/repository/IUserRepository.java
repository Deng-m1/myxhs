/**
 * 仓储服务
 * 1. 定义仓储接口，之后由基础设施层做具体实现
 */
package cn.dbj.domain.userInfo.repository;

import cn.dbj.domain.userInfo.model.aggregate.User;
import cn.dbj.domain.userInfo.model.dto.UserBaseDTO;

import java.util.List;

public interface IUserRepository{
    boolean existsById(Long id);

    public UserBaseDTO getUserInfo(Long id);

    List<UserBaseDTO> getList(Long uid, String type);

    void save(User user);
}