package cn.dbj.domain.userInfo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String name;
    private Integer sex;
    private Integer age;
    private Date date;
    private Integer status;
    private Integer attentionCount;
    private Integer followerCount;
}
