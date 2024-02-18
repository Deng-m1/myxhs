package cn.dbj.infrastructure.follow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("count")
@Data
public class CountDo implements Serializable {
    @TableId
    private Long uid;

    private Long objId;

    private String objType;

    private String countKey;

    private Long countValue;
}
