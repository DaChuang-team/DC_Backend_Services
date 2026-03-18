package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.AdminDTO;
import org.dachuang_team.dc_backend_services.services.AdminService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制层
 * 提供管理员注册、登录以及查询相关的接口
 */
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    /**
     * 管理员注册接口
     *
     * @param adminDTO 接收 JSON 格式的注册信息
     * @return 响应结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody AdminDTO adminDTO) {
        try {
            // 调用服务层进行注册
            adminService.registerAdmin(adminDTO);
            return Result.success("管理员注册成功: " + adminDTO.getAdminName(), null);
        } catch (IllegalArgumentException e) {
            // 处理业务异常
            return Result.error(400, "注册失败: " + e.getMessage());
        } catch (Exception e) {
            // 处理服务器错误
            return Result.error(500, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 管理员登录接口
     *
     * @param adminDTO 接收 JSON 格式的登录信息
     * @return 登录结果及管理员基本信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminDTO adminDTO) {
        try{
            // 1. 调用服务层验证用户名和密码
            String token = adminService.authenticateAdmin(adminDTO.getAdminName(), adminDTO.getAdminPassword());
            if (token != null) {
                // 2. 验证成功，获取管理员详细信息
                AdminDTO admin = adminService.getAdminByAdminName(adminDTO.getAdminName());

                // 3. 构造返回给前端的数据
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", token);
                responseBody.put("adminName", admin.getAdminName());
                responseBody.put("adminRole", admin.getAdminRole());
                return Result.success("登录成功", responseBody);
            }else {
                // 4. 验证失败
                return Result.error(401, "用户名或密码错误");
            }
        }catch (Exception e) {
            return Result.error(500, "登录时发生服务器错误");
        }

    }

    /**
     * 获取所有管理员信息接口
     *
     * @return 格式化后的管理员列表
     */
    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllAdmins() {
        // 1. 调用服务层获取所有管理员 DTO 列表
        List<AdminDTO> adminList = adminService.getAllAdmins();

        // 2. 准备返回的数据列表
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 定义日期格式：年-月-日
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AdminDTO admin : adminList) {
            Map<String, Object> adminMap = new HashMap<>();

            // 映射字段 (按照 Navicat 中的字段名)
            adminMap.put("admin_id", admin.getAdminId());
            adminMap.put("admin_name", admin.getAdminName());
            adminMap.put("admin_role", admin.getAdminRole());

            // 格式化日期：最后登录时间
            if (admin.getLastLogin() != null) {
                adminMap.put("last_login", admin.getLastLogin().format(formatter));
            } else {
                adminMap.put("last_login", "从未登录");
            }

            resultList.add(adminMap);
        }

        // 3. 返回统一 JSON 格式
        return Result.success("获取管理员列表成功", resultList);
    }

    /**
     * 管理员修改用户状态接口
     *
     * @param userName 用户名
     * @param status   新状态 ('正常' 或 '异常')
     * @return 响应结果
     */
    @PutMapping("/updateUserStatus")
    public Result<String> updateUserStatus(@RequestParam String userName, @RequestParam String status) {
        try {
            boolean success = userService.updateUserStatusByAdmin(userName, status);
            if (success) {
                return Result.success("用户状态更新成功", null);
            } else {
                return Result.error(400, "用户状态更新失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 删除管理员接口
     *
     * @param params 包含 targetAdminName, currentAdminName, currentAdminPassword，以JSON格式传递
     * @return 响应结果
     */
    @DeleteMapping("/admindelete")
    public Result<String> deleteAdmin(@RequestBody Map<String, String> params) {
        String targetAdminName = params.get("targetAdminName");
        String currentAdminName = params.get("currentAdminName");
        String currentAdminPassword = params.get("currentAdminPassword");
        try {
            boolean success = adminService.deleteAdmin(targetAdminName, currentAdminName, currentAdminPassword);
            if (success) {
                return Result.success("管理员删除成功", null);
            } else {
                return Result.error(400, "管理员删除失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }
}
