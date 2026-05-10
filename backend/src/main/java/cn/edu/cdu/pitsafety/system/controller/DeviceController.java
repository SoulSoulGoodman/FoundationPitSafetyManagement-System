package cn.edu.cdu.pitsafety.system.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import cn.edu.cdu.pitsafety.system.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceInfoService deviceInfoService;

    @GetMapping("/list")
    public Result<List<DeviceInfo>> list() {
        List<DeviceInfo> list = deviceInfoService.getDeviceList();
        return Result.success(list);
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
}