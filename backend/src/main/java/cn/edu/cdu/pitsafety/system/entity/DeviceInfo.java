package cn.edu.cdu.pitsafety.system.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceInfo {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private Long ownerId;
    private String ipGrade;
    private Integer maintenanceCycle;
    private Integer status;
    private LocalDateTime createTime;
}