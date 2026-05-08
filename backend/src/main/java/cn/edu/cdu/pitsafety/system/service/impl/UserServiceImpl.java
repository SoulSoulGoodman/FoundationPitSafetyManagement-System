package cn.edu.cdu.pitsafety.system.service.impl;

import cn.edu.cdu.pitsafety.security.JwtTokenProvider;
import cn.edu.cdu.pitsafety.system.dto.*;
import cn.edu.cdu.pitsafety.system.entity.SysRole;
import cn.edu.cdu.pitsafety.system.entity.SysUser;
import cn.edu.cdu.pitsafety.system.entity.SysUserRole;
import cn.edu.cdu.pitsafety.system.mapper.SysRoleMapper;
import cn.edu.cdu.pitsafety.system.mapper.SysUserMapper;
import cn.edu.cdu.pitsafety.system.mapper.SysUserRoleMapper;
import cn.edu.cdu.pitsafety.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SysUser user = userMapper.findByUsername(request.getUsername());
        List<SysRole> roles = userRoleMapper.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        String token = jwtTokenProvider.generateToken(user.getUsername(), roleCodes);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .roles(roleCodes)
                .build();

        return LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<SysUser> users = userMapper.findAll();
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        userMapper.insert(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return convertToVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        SysUser user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.update(user);

        if (request.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(id);
            for (Long roleId : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return convertToVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRoleMapper.deleteByUserId(id);
        userMapper.delete(id);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        userMapper.updatePassword(id, passwordEncoder.encode(newPassword));
    }

    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        List<SysRole> roles = userRoleMapper.findRolesByUserId(user.getId());
        vo.setRoles(roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toList()));

        return vo;
    }
}
