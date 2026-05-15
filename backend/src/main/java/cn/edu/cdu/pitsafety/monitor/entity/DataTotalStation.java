// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/entity/DataTotalStation.java
package cn.edu.cdu.pitsafety.monitor.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataTotalStation {
    private Long id;
    private String sensorCode;
    private LocalDateTime collectTime;
    private Double deltaX;
    private Double deltaY;
    private Double deltaH;
    private Double totalX;
    private Double totalY;
    private Double totalH;
    private Double temperature;
    private LocalDateTime createTime;
}
