package cn.edu.cdu.pitsafety.system.controller;

import cn.edu.cdu.pitsafety.common.Result;
import cn.edu.cdu.pitsafety.system.dto.*;
import cn.edu.cdu.pitsafety.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

   @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
       try {
           LoginResponse response = userService.login(request);
           return Result.success(response);
       } catch (Exception e) {
           return Result.error(401, e.getMessage());
       }
   }
    @GetMapping("/users")
    public Result<List<UserVO>> getAllUsers() {
        try {
            List<UserVO> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO user = userService.getUserById(id);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(404, e.getMessage());
        }
    }

    @PostMapping("/users")
    public Result<UserVO> createUser(@RequestBody UserCreateRequest request) {
        try {
            UserVO user = userService.createUser(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            UserVO user = userService.updateUser(id, request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/users/{id}/change-password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            userService.changePassword(id, oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
