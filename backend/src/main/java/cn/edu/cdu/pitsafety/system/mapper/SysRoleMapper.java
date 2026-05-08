package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.SysRole;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SysRoleMapper {

    @Select("SELECT * FROM sys_role")
    List<SysRole> findAll();

    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    SysRole findById(Long id);

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole findByRoleCode(String roleCode);

    @Insert("INSERT INTO sys_role(role_name, role_code) VALUES(#{roleName}, #{roleCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    @Update("UPDATE sys_role SET role_name = #{roleName} WHERE id = #{id}")
    int update(SysRole role);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int delete(Long id);
}
