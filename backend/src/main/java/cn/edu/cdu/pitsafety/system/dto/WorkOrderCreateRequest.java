package cn.edu.cdu.pitsafety.system.dto;

import lombok.Data;

@Data
public class WorkOrderCreateRequest {
    private Long deviceId;
    private Long creatorId;
    private String faultDesc;
}