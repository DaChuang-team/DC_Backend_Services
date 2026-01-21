package org.dachuang_team.dc_backend_services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        System.out.println("****UC-TEST**** UserDTO info received: " + userDTO.toString());
        try {
            userService.registerUser(userDTO);
            return ResponseEntity.status(200).body("注册成功" + userDTO.getUserName());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("注册失败: " + e.getMessage());
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        boolean isAuthenticated = userService.authenticateUser(userDTO.getUserName(), userDTO.getUserPassword());
        if (isAuthenticated) {
            User_General user = userService.getUserByUserName(userDTO.getUserName());
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userName", user.getUserName());
            responseBody.put("userPhone", user.getUserPhone());
            responseBody.put("userGender", user.getUserGender());
            responseBody.put("userPermissions", user.getUserPermissions());
            responseBody.put("userBirthday", user.getUserBirthday());
            try{
                ObjectMapper mapper = new ObjectMapper();
                String jsonResponse = mapper.writeValueAsString(responseBody);
                return ResponseEntity.status(201).body(jsonResponse);
            } catch (Exception e){
                return ResponseEntity.status(500).body("服务器错误: " + e.getMessage());
            }


        } else {
            return ResponseEntity.status(401).body("用户名或密码错误");
        }
    }

    @PutMapping("/updateInfo")
    public ResponseEntity<String> updateUserInfo(
            @RequestParam String userName,
            @RequestBody userUpdateDTO userUpdateDTO) {
        try {
            System.out.println("<UC-UPD-TEST>start update user info for: " + userName);
            // 更新用户信息
            userService.updateInfo(userName, userUpdateDTO);
            if(userUpdateDTO.getUserName() != null){
                userName = userUpdateDTO.getUserName(); // 如果用户名被更新，使用新的用户名获取信息
            }
            User_General updatedUser = userService.getUserByUserName(userName);
            System.out.println("<UC-UPD-TEST>" + userUpdateDTO.toString());
            // 构造 JSON 响应
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userName", updatedUser.getUserName());
            responseBody.put("userPhone", updatedUser.getUserPhone());
            responseBody.put("userGender", updatedUser.getUserGender());
            responseBody.put("userPermissions", updatedUser.getUserPermissions());
            responseBody.put("userBirthday", updatedUser.getUserBirthday());
            responseBody.put("userPreference", updatedUser.getUserPreference());
            responseBody.put("userAvatarURL", updatedUser.getUserAvatarURL());
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(responseBody);
            return ResponseEntity.status(202).body(jsonResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(402).body("用户信息更新失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("服务器错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户信息接口
     * 参考 login 和 register 接口的实现风格
     * 修改要求：
     * 1. 字段名对应 Navicat 中的名称 (user_id, create_time, last_login, user_gender, user_name, user_preference, user_phone)
     * 2. 日期显示为年月日 (yyyy-MM-dd)
     * 3. 角色固定为 "普通用户"
     * @return 包含特定用户信息的 JSON 列表
     */
    @GetMapping("/all")
    public ResponseEntity<String> getAllUsers() {
        try {
            // 从 service 层获取所有用户实体
            List<User_General> users = userService.getAllUsers();
            
            // 定义日期格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            // 构造用于返回的列表，仅保留需要的字段，并使用 Navicat 风格的键名
            List<Map<String, Object>> responseList = new ArrayList<>();
            
            for (User_General user : users) {
                Map<String, Object> userMap = new HashMap<>();
                
                // 对应 Navicat 字段名
                userMap.put("user_id", user.getUserId());
                userMap.put("user_name", user.getUserName());
                userMap.put("user_phone", user.getUserPhone());
                userMap.put("user_gender", user.getUserGender());
                userMap.put("user_preference", user.getUserPreference());
                
                // 角色固定显示为 "普通用户"
                userMap.put("user_role", "普通用户");
                
                // 格式化创建时间 (create_time)，仅显示年月日
                if (user.getCreateTime() != null) {
                    userMap.put("create_time", user.getCreateTime().format(formatter));
                } else {
                    userMap.put("create_time", null);
                }
                
                // 格式化最后登录时间 (last_login)，仅显示年月日
                if (user.getLastLoginAt() != null) {
                    userMap.put("last_login", user.getLastLoginAt().format(formatter));
                } else {
                    userMap.put("last_login", null);
                }
                
                responseList.add(userMap);
            }
            
            // 使用 ObjectMapper 将列表转换为 JSON 字符串
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(responseList);
            
            // 返回 200 OK 状态码和 JSON 响应体
            return ResponseEntity.status(200).body(jsonResponse);
        } catch (Exception e) {
            // 异常处理，返回 500 服务器错误
            return ResponseEntity.status(500).body("获取用户信息失败: " + e.getMessage());
        }
    }

}