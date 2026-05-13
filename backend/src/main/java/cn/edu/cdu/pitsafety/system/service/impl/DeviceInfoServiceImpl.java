package cn.edu.cdu.pitsafety.system.service.impl;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import cn.edu.cdu.pitsafety.system.mapper.DeviceInfoMapper;
import cn.edu.cdu.pitsafety.system.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Override
    public Map<String, Object> getDeviceList(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<DeviceInfo> list = deviceInfoMapper.selectList(keyword, status, offset, pageSize);
        int total = deviceInfoMapper.selectCount(keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean addDevice(DeviceInfo deviceInfo) {
        return deviceInfoMapper.insert(deviceInfo) > 0;
    }

    @Override
    public boolean updateDeviceStatus(Long id, Integer status) {
        return deviceInfoMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean updateDevice(DeviceInfo deviceInfo) {
        return deviceInfoMapper.updateById(deviceInfo) > 0;
    }

    @Override
    public boolean deleteDevice(Long id) {
        return deviceInfoMapper.deleteById(id) > 0;
    }
}