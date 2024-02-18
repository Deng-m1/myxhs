package cn.dbj.model.Do;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
@TableName("count")
@Data
public class CountDo implements Serializable {
    @TableId
    private Long uid;
    //如果是帖子，则是帖子id
    private String objId;
    //表示是那种大类型
    /*private String objType;*/
    //小类型。例如帖子里的点赞或者是自身的关注
    private String countKey;

    private Long countValue;
}
