package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
//        System.out.println("****UC-TEST**** UserDTO info received: " + userDTO.toString());
        try {
            userService.registerUser(userDTO);
            // 返回 JSON 格式的成功信息
            return Result.success("注册成功: " + userDTO.getUserName(), null);
        } catch (Exception e) {
            // 返回 JSON 格式的错误信息
            return Result.error(400, "注册失败: " + e.getMessage());
        }
    }

    // 用户登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        try {
            String token = userService.authenticateUser(userDTO.getUserName(), userDTO.getUserPassword());
            if (token != null) { //如果身份验证成功，返回用户信息和 token
                User_General user = userService.getUserByUserName(userDTO.getUserName());
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", token);
                responseBody.put("userName", user.getUserName());
                responseBody.put("userPhone", user.getUserPhone());
                responseBody.put("userGender", user.getUserGender());
                responseBody.put("userPermissions", user.getUserPermissions());
                responseBody.put("userBirthday", user.getUserBirthday());
                responseBody.put("userStatus", user.getUserStatus());

                // 直接返回 Result 对象，Spring 会自动转为 JSON
                return Result.success("登录成功", responseBody);
            } else {
                return Result.error(401, "用户名或密码错误");
            }
        } catch (IllegalArgumentException e) {
            // 捕获状态异常导致的登录失败
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "登录时发生服务器错误");
        }
    }

    @PutMapping("/updateInfo")
    public Result<Map<String, Object>> updateUserInfo(@RequestBody userUpdateDTO userUpdateDTO) {
        try {
            // 1. 从安全上下文中获取 Filter 存入的 userId
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 2. 调用业务层更新信息
            userService.updateInfo(currentUserId, userUpdateDTO);

            // 3. 获取更新后的用户信息
            User_General updatedUser = userService.getUserById(currentUserId);
            Map<String, Object> responseBody = buildUserResponse(updatedUser);

            return Result.success("更新成功", responseBody);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "用户信息更新失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage());
        }
    }



    /**
     * 获取所有用户信息接口
     * 参考 login 和 register 接口的实现风格
     */
    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllUsers() {
        // 1. 获取所有用户列表
        List<User_General> userList = userService.getAllUsers();
        
        // 2. 准备返回的数据列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // 定义日期格式：年-月-日
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (User_General user : userList) {
            Map<String, Object> userMap = new HashMap<>();
            
            // 映射字段 (按照 Navicat 中的字段名 and 用户要求)
            userMap.put("user_id", user.getUserId()); // 明确包含 userId
            userMap.put("user_name", user.getUserName());
            userMap.put("user_phone", user.getUserPhone());
            userMap.put("user_gender", user.getUserGender());
            // 角色固定显示为 "普通用户"
            userMap.put("user_permissions", "普通用户");
            userMap.put("user_birthday", user.getUserBirthday());
            userMap.put("user_preference", user.getUserPreference());
            userMap.put("user_avatar_url", user.getUserAvatarURL());
            userMap.put("user_status", user.getUserStatus());
            
            // 格式化日期：创建时间
            if (user.getCreateTime() != null) {
                userMap.put("create_time", user.getCreateTime().format(formatter));
            } else {
                userMap.put("create_time", "未知");
            }
            
            // 格式化日期：最后登录时间
            if (user.getLastLoginAt() != null) {
                userMap.put("last_login_at", user.getLastLoginAt().format(formatter));
            } else {
                userMap.put("last_login_at", "从未登录");
            }

            resultList.add(userMap);
        }

        // 3. 返回统一 JSON 格式
        return Result.success("获取成功", resultList);
    }

    /**
     * 删除用户接口
     * @param userName 用户名
     * @return 响应结果
     */
    @DeleteMapping("/delete")
    public Result<String> deleteUser(@RequestParam String userName) {
        try {
            boolean success = userService.deleteUser(userName);
            if (success) {
                return Result.success("用户删除成功", null);
            } else {
                return Result.error(400, "用户删除失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    // 辅助方法：构建用户信息的响应数据
    private Map<String, Object> buildUserResponse(User_General user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", user.getUserName());
        map.put("userPhone", user.getUserPhone());
        map.put("userGender", user.getUserGender());
        map.put("userPermissions", user.getUserPermissions());
        map.put("userBirthday", user.getUserBirthday());
        map.put("userPreference", user.getUserPreference());
        map.put("userAvatarURL", user.getUserAvatarURL());
        map.put("userStatus", user.getUserStatus());
        return map;
    }
}