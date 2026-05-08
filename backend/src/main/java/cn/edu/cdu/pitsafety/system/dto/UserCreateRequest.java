package cn.edu.cdu.pitsafety.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private List<Long> roleIds;
}
