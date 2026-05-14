package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.AdminDTO;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.domain.VO.*;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.services.AdminService;
import org.dachuang_team.dc_backend_services.services.MerchantService;
import org.dachuang_team.dc_backend_services.services.OrderService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderService orderService;

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
    @PostMapping("/admindelete")
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

    /**
     * 获取所有商户信息接口
     *
     * @return 商户列表
     */
    @GetMapping("/merchants/all")
    public Result<List<MerchantAdminVO>> getAllMerchants() {
        try {
            List<MerchantAdminVO> merchants = merchantService.getAllMerchants();
            return Result.success("获取商户列表成功", merchants);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 管理员修改商户状态接口
     *
     * @param merchantId 商户ID
     * @param status     新状态 (0-待审核，1-审核通过，2-审核不通过，3-已冻结)
     * @return 响应结果
     */
    @PutMapping("/merchants/updateStatus")
    public Result<String> updateMerchantStatus(@RequestParam Long merchantId, @RequestParam Integer status) {
        try {
            boolean success = merchantService.updateMerchantStatusByAdmin(merchantId, status);
            if (success) {
                return Result.success("商户状态更新成功", null);
            } else {
                return Result.error(400, "商户状态更新失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取全部订单信息接口
     *
     * @param status 订单状态（可选）
     * @param page   页码（默认1）
     * @param size   每页数量（默认10）
     * @return 订单列表
     */
    @GetMapping("/orders/all")
    public Result<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
            Page<Order> orderPage = orderService.getAllOrders(status, pageable);

            List<OrderVO> orderVOList = orderPage.getContent().stream()
                    .map(OrderVO::from)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderVOList);
            response.put("totalItems", orderPage.getTotalElements());
            response.put("totalPages", orderPage.getTotalPages());
            response.put("currentPage", orderPage.getNumber() + 1);

            return Result.success("获取订单列表成功", response);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取近7天、30天和90天的订单量和销售额统计接口
     *
     * @return 订单统计信息
     */
    @GetMapping("/stats/orderStats")
    public Result<Map<String, OrderStatsVO>> getOrderStats() {
        try {
            Map<String, OrderStatsVO> stats = adminService.getOrderStatsForPeriods();
            return Result.success("获取订单统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取产品品类和对应数量统计接口
     *
     * @return 产品品类统计信息
     */
    @GetMapping("/stats/productCategoryStats")
    public Result<List<ProductCategoryStatsVO>> getProductCategoryStats() {
        try {
            List<ProductCategoryStatsVO> stats = adminService.getProductCategoryStats();
            return Result.success("获取产品品类统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取近6个月每月的注册用户数、订单数、销售额和新商品数统计接口
     *
     * @return 近6个月每月统计信息列表
     */
    @GetMapping("/stats/sixMonthStats")
    public Result<List<MonthlyStatsVO>> getSixMonthStats() {
        try {
            List<MonthlyStatsVO> stats = adminService.getSixMonthStats();
            return Result.success("获取近6个月统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取最近7天每天的订单量和销售额统计接口
     *
     * @return 最近7天每日订单统计信息
     */
    @GetMapping("/stats/dailyOrderStats")
    public Result<List<DailyOrderStatsVO>> getDailyOrderStats() {
        try {
            List<DailyOrderStatsVO> stats = adminService.getDailyOrderStatsForLast7Days();
            return Result.success("获取每日订单统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }
}
