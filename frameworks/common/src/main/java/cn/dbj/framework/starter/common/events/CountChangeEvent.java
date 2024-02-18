package cn.dbj.framework.starter.common.events;


import cn.dbj.framework.starter.common.domain.constant.DomainEventType;
import cn.dbj.framework.starter.common.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.io.Serializable;



import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("COUNT_CHANGE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class CountChangeEvent extends DomainEvent implements Serializable {
    /*private Long userId;*/
    private Long uid;
    private String objId;
    private String countKey;
    private Long countValue;
    public CountChangeEvent(Long uid, String objId, String countKey, Long countValue)
    {
        super(DomainEventType.CountChangeEvent);
        this.uid=uid;
        this.objId=objId;
        this.countKey=countKey;
        this.countValue=countValue;
        System.out.println("CountChangeEvent"+countKey+"   -----------   "+this.getId());
    }

}
