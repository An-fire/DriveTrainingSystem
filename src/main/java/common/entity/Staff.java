package common.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Staff {
    private String id;
    private String name;
    private String phone;
    private String password;
    private String role;
    private String subject;
    private Date createTime;
}