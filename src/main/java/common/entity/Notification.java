package common.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Notification {
    private String id;
    private String userId;
    private String type;
    private String content;
    private Integer isRead;   // 0未读 1已读
    private Date createTime;
}