// backend/src/main/java/cn/edu/cdu/pitsafety/dashboard/controller/DashboardController.java
package cn.edu.cdu.pitsafety.dashboard.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // 设备状态统计
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getDeviceStats() {
        return dashboardService.getDeviceStats();
    }

    // 近7天每日告警数
    @GetMapping("/alert-trend")
    public Result<List<Map<String, Object>>> getAlertTrend() {
        return dashboardService.getAlertTrend();
    }

    // 最近10条异常
    @GetMapping("/recent-alerts")
    public Result<List<Map<String, Object>>> getRecentAlerts() {
        return dashboardService.getRecentAlerts();
    }

    // 待处理工单
    @GetMapping("/pending-orders")
    public Result<List<Map<String, Object>>> getPendingOrders() {
        return dashboardService.getPendingOrders();
    }
}
