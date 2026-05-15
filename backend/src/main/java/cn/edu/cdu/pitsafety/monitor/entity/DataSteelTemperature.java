// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/entity/DataSteelTemperature.java
package cn.edu.cdu.pitsafety.monitor.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataSteelTemperature {
    private Long id;
    private String sensorCode;
    private LocalDateTime collectTime;
    private Double temperature;
    private Double measureVal;
    private LocalDateTime createTime;
}
