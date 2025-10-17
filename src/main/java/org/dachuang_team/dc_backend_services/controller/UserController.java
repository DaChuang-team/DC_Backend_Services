package org.dachuang_team.dc_backend_services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        //System.out.println("****UC-TEST**** UserDTO info received: " + userDTO.toString());
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
}