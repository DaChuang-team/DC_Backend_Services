package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;


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
            return ResponseEntity.status(201).body("用户: " + userDTO.getUserName() + " 登录成功");

        } else {
            return ResponseEntity.status(401).body("用户名或密码错误");
        }
    }
}