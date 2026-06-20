package common.entity;
import lombok.Data;
import java.sql.Time;
import java.util.Date;

@Data
public class CoachSchedule {
    private String id;
    private String coachId;
    private Integer weekday;   // 1-7
    private Time startTime;
    private Time endTime;
    private Date createTime;
}