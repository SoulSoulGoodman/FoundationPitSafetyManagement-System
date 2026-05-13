package cn.edu.cdu.pitsafety.system.service;

import cn.edu.cdu.pitsafety.system.entity.WorkOrder;
import java.util.List;
import java.util.Map;

public interface WorkOrderService {

    // 工单列表（分页+筛选）
    Map<String, Object> getWorkOrderList(Integer status, Long deviceId, Long repairerId, int page, int pageSize);

    // 工单详情
    WorkOrder getWorkOrderById(Long id);

    // 创建工单
    boolean createWorkOrder(Long deviceId, Long creatorId, String faultDesc);

    // 指派维修工
    boolean assignRepairer(Long id, Long repairerId);

    // 开始维修（到场签到）
    boolean startRepair(Long id);

    // 完成维修（填日志）
    boolean completeRepair(Long id, String repairLog);

    // 验收通过
    boolean acceptWorkOrder(Long id);

    // 取消工单
    boolean cancelWorkOrder(Long id);
}