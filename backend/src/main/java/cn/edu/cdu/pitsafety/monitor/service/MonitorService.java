// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/service/MonitorService.java
package cn.edu.cdu.pitsafety.monitor.service;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.monitor.entity.*;

import java.util.List;
import java.util.Map;

public interface MonitorService {

    Result<Map<String, Object>> getTotalStationData(String sensorCode, String startTime, String endTime, int page, int pageSize);

    Result<Map<String, Object>> getAxialForceData(String sensorCode, String startTime, String endTime, int page, int pageSize);

    Result<Map<String, Object>> getSteelTemperatureData(String sensorCode, String startTime, String endTime, int page, int pageSize);

    Result<List<String>> getAllSensorCodes();
}
