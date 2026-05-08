package cn.edu.cdu.pitsafety.system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private List<String> roles;
}
