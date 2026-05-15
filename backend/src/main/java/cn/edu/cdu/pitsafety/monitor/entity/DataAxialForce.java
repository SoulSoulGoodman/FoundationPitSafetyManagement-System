// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/entity/DataAxialForce.java
package cn.edu.cdu.pitsafety.monitor.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataAxialForce {
    private Long id;
    private String sensorCode;
    private LocalDateTime collectTime;
    private Double wForce;
    private Double fPosition;
    private Double temperature;
    private LocalDateTime createTime;
}
