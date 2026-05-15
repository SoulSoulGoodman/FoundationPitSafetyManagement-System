// backend/src/main/java/cn/edu/cdu/pitsafety/monitor/mapper/DataSteelTemperatureMapper.java
package cn.edu.cdu.pitsafety.monitor.mapper;

import cn.edu.cdu.pitsafety.monitor.entity.DataSteelTemperature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataSteelTemperatureMapper {
    
    @Select("SELECT * FROM data_steel_temperature WHERE sensor_code = #{sensorCode} " +
            "AND collect_time BETWEEN #{startTime} AND #{endTime} ORDER BY collect_time")
    List<DataSteelTemperature> findBySensorAndTime(@Param("sensorCode") String sensorCode,
        @Param("startTime") String startTime, @Param("endTime") String endTime);
        
    @Select("SELECT DISTINCT sensor_code FROM data_steel_temperature")
    List<String> findAllSensorCodes();
}
