// backend/src/main/java/cn/edu/cdu/pitsafety/dashboard/dto/DeviceStatusStat.java
package cn.edu.cdu.pitsafety.dashboard.dto;

import lombok.Data;

@Data
public class DeviceStatusStat {
    private Integer status;
    private Long count;
}
