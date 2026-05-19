package cn.edu.cdu.pitsafety.system.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkOrder {
    private Long id;
    private String orderNo;
    private Long deviceId;
    private String deviceCode;
    private Long creatorId;
    private Long repairerId;
    private String faultDesc;
    private String repairLog;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}