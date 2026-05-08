package cn.edu.cdu.pitsafety.system.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
