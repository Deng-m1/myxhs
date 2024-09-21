package cn.dbj.types.command;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleCommentResponse {

    private boolean success;
    private String message;
    private Integer commentId;

}
