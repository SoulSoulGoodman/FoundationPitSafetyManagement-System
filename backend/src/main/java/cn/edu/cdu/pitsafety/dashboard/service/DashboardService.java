// backend/src/main/java/cn/edu/cdu/pitsafety/dashboard/service/DashboardService.java
package cn.edu.cdu.pitsafety.dashboard.service;

import cn.edu.cdu.pitsafety.common.Result;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    Result<List<Map<String, Object>>> getDeviceStats();

    Result<List<Map<String, Object>>> getAlertTrend();

    Result<List<Map<String, Object>>> getRecentAlerts();

    Result<List<Map<String, Object>>> getPendingOrders();
}
