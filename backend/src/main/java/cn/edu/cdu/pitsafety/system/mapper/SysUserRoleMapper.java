package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.SysRole;
import cn.edu.cdu.pitsafety.system.entity.SysUserRole;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> findByUserId(Long userId);
    
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRole> findRolesByUserId(Long userId);
    
    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUserRole userRole);
    
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
    
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
