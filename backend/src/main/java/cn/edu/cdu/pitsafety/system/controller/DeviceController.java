package cn.edu.cdu.pitsafety.system.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import cn.edu.cdu.pitsafety.system.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceInfoService deviceInfoService;

    // 查询设备列表（支持分页 + 筛选）
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = deviceInfoService.getDeviceList(keyword, status, page, pageSize);
        return Result.success(data);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody DeviceInfo deviceInfo) {
        boolean success = deviceInfoService.addDevice(deviceInfo);
        if (success) {
            return Result.success("新增成功");
        } else {
            return Result.error(400, "新增失败");
        }
    }

    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = deviceInfoService.updateDeviceStatus(id, status);
        if (success) {
            return Result.success("状态修改成功");
        } else {
            return Result.error(400, "状态修改失败");
        }
    }

    // 编辑设备完整信息
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody DeviceInfo deviceInfo) {
        deviceInfo.setId(id);
        boolean success = deviceInfoService.updateDevice(deviceInfo);
        if (success) {
            return Result.success("编辑成功");
        } else {
            return Result.error(400, "编辑失败");
        }
    }

    // 删除设备
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = deviceInfoService.deleteDevice(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error(400, "删除失败");
        }
    }
}