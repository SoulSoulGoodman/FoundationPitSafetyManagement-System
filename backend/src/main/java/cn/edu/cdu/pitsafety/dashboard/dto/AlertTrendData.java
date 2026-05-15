// backend/src/main/java/cn/edu/cdu/pitsafety/dashboard/dto/AlertTrendData.java
package cn.edu.cdu.pitsafety.dashboard.dto;

import lombok.Data;

@Data
public class AlertTrendData {
    private String date;
    private Long count;
}
