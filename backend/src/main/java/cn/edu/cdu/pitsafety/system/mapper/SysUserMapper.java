package cn.edu.cdu.pitsafety.system.mapper;

import cn.edu.cdu.pitsafety.system.entity.SysUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SysUserMapper {
    
    @Select("SELECT * FROM sys_user")
    List<SysUser> findAll();
    
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser findById(Long id);
    
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);
    
    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    SysUser findByPhone(String phone);
    
    @Insert("INSERT INTO sys_user(username, password, real_name, phone, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{phone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);
    
    @Update("UPDATE sys_user SET real_name = #{realName}, phone = #{phone}, " +
            "status = #{status} WHERE id = #{id}")
    int update(SysUser user);
    
    @Update("UPDATE sys_user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsername(String username);
}
