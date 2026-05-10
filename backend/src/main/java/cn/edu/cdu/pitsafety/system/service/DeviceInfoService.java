package cn.edu.cdu.pitsafety.system.service;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import java.util.List;

public interface DeviceInfoService {

    List<DeviceInfo> getDeviceList();

    boolean addDevice(DeviceInfo deviceInfo);

    boolean updateDeviceStatus(Long id, Integer status);
}