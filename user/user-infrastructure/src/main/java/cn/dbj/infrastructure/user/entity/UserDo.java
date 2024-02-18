package cn.dbj.infrastructure.user.entity;

import cn.dbj.domain.userInfo.model.valobj.UserAccountType;
import cn.dbj.framework.starter.database.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import static lombok.AccessLevel.PRIVATE;


@TableName("user")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDo extends BaseDO implements Serializable {

        /**
         * 用户ID
         */

        @TableId
        private Long id;

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
        private String status;

        /**
         * 用户头像URL
         */
        private String imageUrl;

        /**
         * 用户描述或简介
         */
        private String description;

        // Getters and setters
}
