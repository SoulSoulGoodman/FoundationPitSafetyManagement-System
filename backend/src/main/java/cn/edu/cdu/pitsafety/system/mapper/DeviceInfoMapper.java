package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceInfoMapper {

    // 分页查询设备列表（支持筛选）
    List<DeviceInfo> selectList(@Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);

    // 查询总数（用于分页）
    int selectCount(@Param("keyword") String keyword,
                    @Param("status") Integer status);

    int insert(DeviceInfo deviceInfo);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 编辑设备完整信息
    int updateById(DeviceInfo deviceInfo);

    // 删除设备
    int deleteById(@Param("id") Long id);

    // 获取设备状态统计（大屏用）
    List<Map<String, Object>> getDeviceStatusStats();

    // 获取最近的异常设备（大屏用）
    List<Map<String, Object>> getRecentAbnormalDevices();
}
