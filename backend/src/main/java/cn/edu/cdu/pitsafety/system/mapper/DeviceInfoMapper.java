package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.DeviceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DeviceInfoMapper {

    List<DeviceInfo> selectList();

    int insert(DeviceInfo deviceInfo);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}