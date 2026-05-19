package cn.edu.cdu.pitsafety.system.service.impl;

import cn.edu.cdu.pitsafety.system.entity.WorkOrder;
import cn.edu.cdu.pitsafety.system.mapper.WorkOrderMapper;
import cn.edu.cdu.pitsafety.system.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    // 生成工单流水号：WO + yyyyMMdd + 3位序号
    private String generateOrderNo() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxOrderNo = workOrderMapper.selectMaxOrderNoByDate("WO" + datePrefix);
        int seq = 1;
        if (maxOrderNo != null && maxOrderNo.length() >= 13) {
            String seqStr = maxOrderNo.substring(11);
            seq = Integer.parseInt(seqStr) + 1;
        }
        return String.format("WO%s%03d", datePrefix, seq);
    }

    @Override
    public Map<String, Object> getWorkOrderList(Integer status, Long deviceId, Long repairerId, Long creatorId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<WorkOrder> list = workOrderMapper.selectList(status, deviceId, repairerId, creatorId, offset, pageSize);
        int total = workOrderMapper.selectCount(status, deviceId, repairerId, creatorId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public WorkOrder getWorkOrderById(Long id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    public boolean createWorkOrder(Long deviceId, Long creatorId, String faultDesc) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderNo(generateOrderNo());
        workOrder.setDeviceId(deviceId);
        workOrder.setCreatorId(creatorId);
        workOrder.setFaultDesc(faultDesc);
        workOrder.setStatus(0);
        return workOrderMapper.insert(workOrder) > 0;
    }

    @Override
    public boolean assignRepairer(Long id, Long repairerId) {
        return workOrderMapper.assignRepairer(id, repairerId) > 0;
    }

    @Override
    public boolean startRepair(Long id) {
        return workOrderMapper.startRepair(id) > 0;
    }

    @Override
    public boolean completeRepair(Long id, String repairLog) {
        return workOrderMapper.completeRepair(id, repairLog) > 0;
    }

    @Override
    public boolean acceptWorkOrder(Long id) {
        return workOrderMapper.acceptWorkOrder(id) > 0;
    }

    @Override
    public boolean cancelWorkOrder(Long id) {
        return workOrderMapper.cancelWorkOrder(id) > 0;
    }
}