package cn.dbj.types.command;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;


@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CreatUserResponse {
    private Long id;

}
