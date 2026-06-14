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
}