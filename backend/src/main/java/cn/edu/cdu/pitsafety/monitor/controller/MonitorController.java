// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/controller/MonitorController.java
package cn.edu.cdu.pitsafety.monitor.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    // 全站仪位移数据查询
    @GetMapping("/total-station")
    public Result<Map<String, Object>> getTotalStationData(
            @RequestParam String sensorCode,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return monitorService.getTotalStationData(sensorCode, startTime, endTime, page, pageSize);
    }

    // 伺服轴力数据查询
    @GetMapping("/axial-force")
    public Result<Map<String, Object>> getAxialForceData(
            @RequestParam String sensorCode,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return monitorService.getAxialForceData(sensorCode, startTime, endTime, page, pageSize);
    }

    // 钢支撑温度数据查询
    @GetMapping("/steel-temperature")
    public Result<Map<String, Object>> getSteelTemperatureData(
            @RequestParam String sensorCode,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return monitorService.getSteelTemperatureData(sensorCode, startTime, endTime, page, pageSize);
    }

    // 获取所有传感器编码列表
    @GetMapping("/sensors")
    public Result<List<String>> getAllSensorCodes() {
        return monitorService.getAllSensorCodes();
    }
}
