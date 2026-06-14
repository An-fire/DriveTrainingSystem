package common.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Enrollment {
    private String id;
    private String studentId;
    private String coachId;
    private String subjectType;
    private String status;
    private Date applyTime;
    private Date auditTime;
}