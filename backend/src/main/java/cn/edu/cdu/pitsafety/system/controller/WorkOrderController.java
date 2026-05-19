package cn.edu.cdu.pitsafety.system.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.system.entity.SysUser;
import cn.edu.cdu.pitsafety.system.entity.WorkOrder;
import cn.edu.cdu.pitsafety.system.dto.WorkOrderCreateRequest;
import cn.edu.cdu.pitsafety.system.mapper.SysUserMapper;
import cn.edu.cdu.pitsafety.system.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private SysUserMapper sysUserMapper;

    // 获取当前登录用户
    private SysUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return sysUserMapper.findByUsername(auth.getName());
    }

    // 获取当前用户角色列表
    private java.util.List<String> currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return java.util.Collections.emptyList();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    // 工单列表（分页+筛选 + 角色自动过滤）
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long repairerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        SysUser user = currentUser();
        java.util.List<String> roles = currentRoles();
        Long filterCreatorId = null;
        Long filterRepairerId = repairerId;

        // 角色过滤：BUYER 只看自己创建的，REPAIRER 只看指派给自己的
        if (roles.contains("ROLE_BUYER") || roles.contains("BUYER")) {
            filterCreatorId = user.getId();
        } else if (roles.contains("ROLE_REPAIRER") || roles.contains("REPAIRER")) {
            filterRepairerId = user.getId();
        }
        // ADMIN 不过滤

        Map<String, Object> data = workOrderService.getWorkOrderList(
                status, deviceId, filterRepairerId, filterCreatorId, page, pageSize);
        return Result.success(data);
    }

    // 工单详情
    @GetMapping("/{id}")
    public Result<WorkOrder> detail(@PathVariable Long id) {
        WorkOrder workOrder = workOrderService.getWorkOrderById(id);
        if (workOrder == null) {
            return Result.error(404, "工单不存在");
        }
        return Result.success(workOrder);
    }

    // 创建工单
    @PostMapping("/create")
    public Result<String> create(@RequestBody WorkOrderCreateRequest request) {
        boolean success = workOrderService.createWorkOrder(request.getDeviceId(), request.getCreatorId(), request.getFaultDesc());
        if (success) {
            return Result.success("工单创建成功");
        } else {
            return Result.error(400, "工单创建失败");
        }
    }

    // 指派维修工 (0→1)
    @PutMapping("/{id}/assign")
    public Result<String> assign(@PathVariable Long id, @RequestParam Long repairerId) {
        boolean success = workOrderService.assignRepairer(id, repairerId);
        if (success) {
            return Result.success("指派成功");
        } else {
            return Result.error(400, "指派失败，请确认工单状态为待派单(0)");
        }
    }

    // 开始维修/到场签到 (1→2)
    @PutMapping("/{id}/start")
    public Result<String> start(@PathVariable Long id) {
        boolean success = workOrderService.startRepair(id);
        if (success) {
            return Result.success("开始维修成功");
        } else {
            return Result.error(400, "开始维修失败，请确认工单状态为待接单(1)");
        }
    }

    // 完成维修填日志 (2→3)
    @PutMapping("/{id}/complete")
    public Result<String> complete(@PathVariable Long id, @RequestParam String repairLog) {
        boolean success = workOrderService.completeRepair(id, repairLog);
        if (success) {
            return Result.success("完成维修成功");
        } else {
            return Result.error(400, "完成维修失败，请确认工单状态为维修中(2)");
        }
    }

    // 验收通过 (3→4)
    @PutMapping("/{id}/accept")
    public Result<String> accept(@PathVariable Long id) {
        boolean success = workOrderService.acceptWorkOrder(id);
        if (success) {
            return Result.success("验收通过");
        } else {
            return Result.error(400, "验收失败，请确认工单状态为待验收(3)");
        }
    }

    // 取消工单 (仅0或1可取消)
    @PutMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id) {
        boolean success = workOrderService.cancelWorkOrder(id);
        if (success) {
            return Result.success("工单已取消");
        } else {
            return Result.error(400, "取消失败，仅待派单或待接单状态的工单可取消");
        }
    }
}