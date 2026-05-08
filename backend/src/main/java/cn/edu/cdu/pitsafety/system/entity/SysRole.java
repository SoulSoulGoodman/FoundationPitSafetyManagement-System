package cn.edu.cdu.pitsafety.system.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysRole {
    private Long id;
    private String roleName;
    private String roleCode;
    private LocalDateTime createTime;
}
