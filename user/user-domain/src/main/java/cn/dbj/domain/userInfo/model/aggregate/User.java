/**
 * 聚合对象；
 * 1. 聚合实体和值对象
 * 2. 聚合是聚合的对象，和提供基础处理对象的方法。但不建议在聚合中引入仓储和接口来做过大的逻辑。而这些复杂的操作应该放到service中处理
 * 3. 对象名称 XxxAggregate
 */
package cn.dbj.domain.userInfo.model.aggregate;

import cn.dbj.domain.userInfo.model.valobj.UserAccountType;
import cn.dbj.framework.starter.common.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class User extends AggregateRoot {
    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户性别，通常使用枚举或约定的数值表示
     */
    private Integer sex;

    /**
     * 用户年龄
     */
    private Integer age;

    /**
     * 用户生日或其他日期信息
     */
    private Date date;

    /**
     * 用户账户类型或状态
     */
    private UserAccountType status;

    /**
     * 用户头像URL
     */
    private String imageUrl;

    /**
     * 用户描述或简介
     */
    private String description;

    private Long attentionCount;

    private Long followerCount;

    public User(Long id, String nickName,String name, String imageUrl, Date birthday, Integer age, Integer sex) {
        this.setCreatStatus();
        this.uid = id;
        this.name = name;
        this.nickName=nickName;
        this.imageUrl = imageUrl;
        this.date = birthday;
        this.age = age;
        this.sex=sex;
    }
    private void setCreatStatus(){
        this.status = UserAccountType.REGULAR;
    }




}