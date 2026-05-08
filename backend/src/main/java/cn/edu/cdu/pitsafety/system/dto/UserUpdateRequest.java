package cn.edu.cdu.pitsafety.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateRequest {
    private String realName;
    private String phone;
    private Integer status;
    private List<Long> roleIds;
}
