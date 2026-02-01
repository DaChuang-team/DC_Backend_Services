package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
            boolean isAuthenticated = userService.authenticateUser(userDTO.getUserName(), userDTO.getUserPassword());
            if (isAuthenticated) {
                User_General user = userService.getUserByUserName(userDTO.getUserName());
                Map<String, Object> responseBody = new HashMap<>();
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
    public Result<Map<String, Object>> updateUserInfo(
            @RequestParam String userName,
            @RequestBody userUpdateDTO userUpdateDTO) {
        try {
//            System.out.println("<UC-UPD-TEST>start update user info for: " + userName);
            // 更新用户信息
            userService.updateInfo(userName, userUpdateDTO);
            if(userUpdateDTO.getUserName() != null){
                userName = userUpdateDTO.getUserName(); // 如果用户名被更新，使用新的用户名获取信息
            }
            User_General updatedUser = userService.getUserByUserName(userName);
            
            // 构造返回数据
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userName", updatedUser.getUserName());
            responseBody.put("userPhone", updatedUser.getUserPhone());
            responseBody.put("userGender", updatedUser.getUserGender());
            responseBody.put("userPermissions", updatedUser.getUserPermissions());
            responseBody.put("userBirthday", updatedUser.getUserBirthday());
            responseBody.put("userPreference", updatedUser.getUserPreference());
            responseBody.put("userAvatarURL", updatedUser.getUserAvatarURL());
            responseBody.put("userStatus", updatedUser.getUserStatus());
            
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

}