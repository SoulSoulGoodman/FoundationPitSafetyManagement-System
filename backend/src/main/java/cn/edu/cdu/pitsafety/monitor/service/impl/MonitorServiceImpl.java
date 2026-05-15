// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/service/impl/MonitorServiceImpl.java
package cn.edu.cdu.pitsafety.monitor.service.impl;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.monitor.entity.*;
import cn.edu.cdu.pitsafety.monitor.mapper.*;
import cn.edu.cdu.pitsafety.monitor.service.MonitorService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private DataTotalStationMapper dataTotalStationMapper;

    @Autowired
    private DataAxialForceMapper dataAxialForceMapper;

    @Autowired
    private DataSteelTemperatureMapper dataSteelTemperatureMapper;

    @Override
    public Result<Map<String, Object>> getTotalStationData(String sensorCode, String startTime, String endTime, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<DataTotalStation> dataList = dataTotalStationMapper.findBySensorAndTime(sensorCode, startTime, endTime);
        PageInfo<DataTotalStation> pageInfo = new PageInfo<>(dataList);

        Map<String, Object> result = new HashMap<>();
        result.put("list", dataList);
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getAxialForceData(String sensorCode, String startTime, String endTime, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<DataAxialForce> dataList = dataAxialForceMapper.findBySensorAndTime(sensorCode, startTime, endTime);
        PageInfo<DataAxialForce> pageInfo = new PageInfo<>(dataList);

        Map<String, Object> result = new HashMap<>();
        result.put("list", dataList);
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getSteelTemperatureData(String sensorCode, String startTime, String endTime, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<DataSteelTemperature> dataList = dataSteelTemperatureMapper.findBySensorAndTime(sensorCode, startTime, endTime);
        PageInfo<DataSteelTemperature> pageInfo = new PageInfo<>(dataList);

        Map<String, Object> result = new HashMap<>();
        result.put("list", dataList);
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<List<String>> getAllSensorCodes() {
        // 根据任务要求，返回所有类型的传感器编码
        List<String> totalStationSensors = dataTotalStationMapper.findAllSensorCodes();
        List<String> axialForceSensors = dataAxialForceMapper.findAllSensorCodes();
        List<String> steelTempSensors = dataSteelTemperatureMapper.findAllSensorCodes();

        Set<String> allSensors = new HashSet<>();
        allSensors.addAll(totalStationSensors);
        allSensors.addAll(axialForceSensors);
        allSensors.addAll(steelTempSensors);

        return Result.success(new ArrayList<>(allSensors));
    }
}
