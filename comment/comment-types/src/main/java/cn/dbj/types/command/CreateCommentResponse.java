package cn.dbj.types.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentResponse {
    private String id;
    private boolean success;
    private String message;
    private Integer commentId;
    public CreateCommentResponse(String id)
    {
        this.id=id;
    }
}
