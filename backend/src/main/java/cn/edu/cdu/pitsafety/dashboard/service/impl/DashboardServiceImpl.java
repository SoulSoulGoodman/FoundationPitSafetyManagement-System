// backend/src/main/java/cn/edu/cdu/pitsafety/dashboard/service/impl/DashboardServiceImpl.java
package cn.edu.cdu.pitsafety.dashboard.service.impl;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.dashboard.service.DashboardService;
import cn.edu.cdu.pitsafety.system.mapper.DeviceInfoMapper;
import cn.edu.cdu.pitsafety.system.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Override
    public Result<List<Map<String, Object>>> getDeviceStats() {
        List<Map<String, Object>> stats = deviceInfoMapper.getDeviceStatusStats();
        return Result.success(stats);
    }

    @Override
    public Result<List<Map<String, Object>>> getAlertTrend() {
        List<Map<String, Object>> trend = workOrderMapper.getAlertTrendLast7Days();
        return Result.success(trend);
    }

    @Override
    public Result<List<Map<String, Object>>> getRecentAlerts() {
        List<Map<String, Object>> alerts = deviceInfoMapper.getRecentAbnormalDevices();
        return Result.success(alerts);
    }

    @Override
    public Result<List<Map<String, Object>>> getPendingOrders() {
        List<Map<String, Object>> orders = workOrderMapper.getPendingWorkOrders();
        return Result.success(orders);
    }
}
