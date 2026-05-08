package cn.edu.cdu.pitsafety.security;

import cn.edu.cdu.pitsafety.system.entity.SysRole;
import cn.edu.cdu.pitsafety.system.entity.SysUser;
import cn.edu.cdu.pitsafety.system.mapper.SysUserMapper;
import cn.edu.cdu.pitsafety.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }

        List<SysRole> roles = userRoleMapper.findRolesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(
                        role.getRoleCode().startsWith("ROLE_") ? role.getRoleCode() : "ROLE_" + role.getRoleCode()))
                .collect(Collectors.toList());

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
