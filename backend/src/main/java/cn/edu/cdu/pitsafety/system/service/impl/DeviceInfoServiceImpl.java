package cn.edu.cdu.pitsafety.system.service.impl;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import cn.edu.cdu.pitsafety.system.mapper.DeviceInfoMapper;
import cn.edu.cdu.pitsafety.system.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Override
    public List<DeviceInfo> getDeviceList() {
        return deviceInfoMapper.selectList();
    }

    @Override
    public boolean addDevice(DeviceInfo deviceInfo) {
        return deviceInfoMapper.insert(deviceInfo) > 0;
    }

    @Override
    public boolean updateDeviceStatus(Long id, Integer status) {
        return deviceInfoMapper.updateStatus(id, status) > 0;
    }
}