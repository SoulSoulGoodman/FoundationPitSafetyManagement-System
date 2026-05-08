package cn.edu.cdu.pitsafety.system.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
}
