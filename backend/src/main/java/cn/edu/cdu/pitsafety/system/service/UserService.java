package cn.edu.cdu.pitsafety.system.service;

import cn.edu.cdu.pitsafety.system.dto.*;

import java.util.List;

public interface UserService {

    LoginResponse login(LoginRequest request);

    List<UserVO> getAllUsers();

    UserVO getUserById(Long id);

    UserVO createUser(UserCreateRequest request);

    UserVO updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    void changePassword(Long id, String oldPassword, String newPassword);
}
