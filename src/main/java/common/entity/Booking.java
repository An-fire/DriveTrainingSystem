package common.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Booking {
    private String id;
    private String studentId;
    private String coachId;
    private String subjectType;
    private Date startTime;
    private Date endTime;
    private String status;
    private Integer studentScore;
    private Integer coachScore;
    private Boolean canExam;
    private Date createTime;
    private String comment;  // 学员对教练的评价内容

    // 附加字段（用于显示）
    private String studentName;  // 学员姓名
    private String coachName;    // 教练姓名
}