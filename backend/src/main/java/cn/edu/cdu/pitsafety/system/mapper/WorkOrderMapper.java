package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WorkOrderMapper {

    // 分页查询工单列表（支持筛选）
    List<WorkOrder> selectList(@Param("status") Integer status,
                               @Param("deviceId") Long deviceId,
                               @Param("repairerId") Long repairerId,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    // 查询工单总数（用于分页）
    int selectCount(@Param("status") Integer status,
                    @Param("deviceId") Long deviceId,
                    @Param("repairerId") Long repairerId);

    // 根据ID查询工单详情
    WorkOrder selectById(@Param("id") Long id);

    // 生成工单号：查询当天最大序号
    String selectMaxOrderNoByDate(@Param("datePrefix") String datePrefix);

    // 创建工单
    int insert(WorkOrder workOrder);

    // 指派维修工 (0 → 1)
    int assignRepairer(@Param("id") Long id, @Param("repairerId") Long repairerId);

    // 开始维修/到场签到 (1 → 2)
    int startRepair(@Param("id") Long id);

    // 完成维修填日志 (2 → 3)
    int completeRepair(@Param("id") Long id, @Param("repairLog") String repairLog);

    // 验收通过 (3 → 4)
    int acceptWorkOrder(@Param("id") Long id);

    // 取消工单 (仅 status 0 或 1 可取消)
    int cancelWorkOrder(@Param("id") Long id);
}