package cn.dbj.types.command;

import lombok.*;

import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CreatUserCommand {
    private Long id;
    private String name;
    private String imageUrl;
    private Date birthday;
    private Integer age;
    private Integer sex;
}
