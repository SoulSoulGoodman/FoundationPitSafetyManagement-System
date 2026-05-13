package cn.edu.cdu.pitsafety.system.service;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import java.util.List;
import java.util.Map;

public interface DeviceInfoService {

    // 查询设备列表（分页+筛选）
    Map<String, Object> getDeviceList(String keyword, Integer status, int page, int pageSize);

    boolean addDevice(DeviceInfo deviceInfo);

    boolean updateDeviceStatus(Long id, Integer status);

    // 编辑设备完整信息
    boolean updateDevice(DeviceInfo deviceInfo);

    // 删除设备
    boolean deleteDevice(Long id);
}