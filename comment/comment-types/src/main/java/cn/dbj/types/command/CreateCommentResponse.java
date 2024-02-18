package cn.dbj.types.command;

import lombok.Data;

@Data
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
