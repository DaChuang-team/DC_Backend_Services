package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.AdminDTO;
import org.dachuang_team.dc_backend_services.domain.PO.OperationLog;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.domain.VO.*;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.services.AdminService;
import org.dachuang_team.dc_backend_services.services.LogService;
import org.dachuang_team.dc_backend_services.services.MerchantService;
import org.dachuang_team.dc_backend_services.services.OrderService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody AdminDTO adminDTO) {
        try {
            adminService.registerAdmin(adminDTO);
            return Result.success("管理员注册成功: " + adminDTO.getAdminName(), null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "注册失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminDTO adminDTO) {
        try {
            String token = adminService.authenticateAdmin(adminDTO.getAdminName(), adminDTO.getAdminPassword());
            if (token != null) {
                AdminDTO admin = adminService.getAdminByAdminName(adminDTO.getAdminName());
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", token);
                responseBody.put("adminName", admin.getAdminName());
                responseBody.put("adminRole", admin.getAdminRole());
                return Result.success("登录成功", responseBody);
            } else {
                return Result.error(401, "用户名或密码错误");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "登录时发生服务器错误");
        }
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllAdmins() {
        List<AdminDTO> adminList = adminService.getAllAdmins();
        List<Map<String, Object>> resultList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AdminDTO admin : adminList) {
            Map<String, Object> adminMap = new HashMap<>();
            adminMap.put("admin_id", admin.getAdminId());
            adminMap.put("admin_name", admin.getAdminName());
            adminMap.put("admin_role", admin.getAdminRole());
            adminMap.put("status", admin.getStatus() != null ? admin.getStatus() : 0);
            if (admin.getLastLogin() != null) {
                adminMap.put("last_login", admin.getLastLogin().format(formatter));
            } else {
                adminMap.put("last_login", "从未登录");
            }
            resultList.add(adminMap);
        }

        return Result.success("获取管理员列表成功", resultList);
    }

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

    @PutMapping("/updateStatus")
    public Result<String> updateAdminStatus(@RequestParam Long adminId, @RequestParam Integer status) {
        try {
            adminService.updateAdminStatus(adminId, status);
            String statusText = status == 0 ? "已启用" : "已禁用";
            return Result.success("管理员 " + statusText, null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @PutMapping("/resetPassword")
    public Result<String> resetAdminPassword(@RequestBody Map<String, Object> request) {
        try {
            Long adminId = Long.valueOf(request.get("adminId").toString());
            String newPassword = (String) request.get("newPassword");
            adminService.resetAdminPassword(adminId, newPassword);
            return Result.success("管理员密码重置成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/logs/business")
    public Result<Map<String, Object>> getBusinessLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
            Page<OperationLog> logPage = logService.getBusinessLogs(module, operator, startDate, endDate, pageable);

            List<Map<String, Object>> logList = logPage.getContent().stream().map(log -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", log.getId());
                m.put("module", log.getModule());
                m.put("action", log.getAction());
                m.put("operator", log.getOperator());
                m.put("operatorId", log.getOperatorId());
                m.put("targetType", log.getTargetType());
                m.put("targetId", log.getTargetId());
                m.put("detail", log.getDetail());
                m.put("result", log.getResult());
                m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("logs", logList);
            response.put("currentPage", logPage.getNumber() + 1);
            response.put("totalItems", logPage.getTotalElements());
            response.put("totalPages", logPage.getTotalPages());

            return Result.success("获取业务日志成功", response);
        } catch (Exception e) {
            return Result.error(500, "获取业务日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/system")
    public Result<Map<String, Object>> getSystemLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
            Page<OperationLog> logPage = logService.getSystemLogs(module, level, startDate, endDate, pageable);

            List<Map<String, Object>> logList = logPage.getContent().stream().map(log -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", log.getId());
                m.put("module", log.getModule());
                m.put("action", log.getAction());
                m.put("operator", log.getOperator());
                m.put("targetType", log.getTargetType());
                m.put("targetId", log.getTargetId());
                m.put("detail", log.getDetail());
                m.put("result", log.getResult());
                m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("logs", logList);
            response.put("currentPage", logPage.getNumber() + 1);
            response.put("totalItems", logPage.getTotalElements());
            response.put("totalPages", logPage.getTotalPages());

            return Result.success("获取系统日志成功", response);
        } catch (Exception e) {
            return Result.error(500, "获取系统日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/business/export")
    public ResponseEntity<byte[]> exportBusinessLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdAt").descending());
            Page<OperationLog> logPage = logService.getBusinessLogs(module, operator, startDate, endDate, pageable);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            writer.write("ID,模块,操作,操作人,操作人ID,目标类型,目标ID,详情,结果,时间\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (OperationLog log : logPage.getContent()) {
                writer.write(log.getId() + ",");
                writer.write(escapeCsv(log.getModule()) + ",");
                writer.write(escapeCsv(log.getAction()) + ",");
                writer.write(escapeCsv(log.getOperator()) + ",");
                writer.write((log.getOperatorId() != null ? String.valueOf(log.getOperatorId()) : "") + ",");
                writer.write(escapeCsv(log.getTargetType()) + ",");
                writer.write(escapeCsv(log.getTargetId()) + ",");
                writer.write(escapeCsv(log.getDetail()) + ",");
                writer.write(escapeCsv(log.getResult()) + ",");
                writer.write(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
                writer.write("\n");
            }
            writer.flush();

            byte[] bytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
            headers.setContentDispositionFormData("attachment", "business_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/logs/system/export")
    public ResponseEntity<byte[]> exportSystemLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdAt").descending());
            Page<OperationLog> logPage = logService.getSystemLogs(module, level, startDate, endDate, pageable);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            writer.write("ID,模块,操作,详情,结果,时间\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (OperationLog log : logPage.getContent()) {
                writer.write(log.getId() + ",");
                writer.write(escapeCsv(log.getModule()) + ",");
                writer.write(escapeCsv(log.getAction()) + ",");
                writer.write(escapeCsv(log.getDetail()) + ",");
                writer.write(escapeCsv(log.getResult()) + ",");
                writer.write(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
                writer.write("\n");
            }
            writer.flush();

            byte[] bytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
            headers.setContentDispositionFormData("attachment", "system_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/merchants/all")
    public Result<List<MerchantAdminVO>> getAllMerchants() {
        try {
            List<MerchantAdminVO> merchants = merchantService.getAllMerchants();
            return Result.success("获取商户列表成功", merchants);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

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

    @GetMapping("/stats/orderStats")
    public Result<Map<String, OrderStatsVO>> getOrderStats() {
        try {
            Map<String, OrderStatsVO> stats = adminService.getOrderStatsForPeriods();
            return Result.success("获取订单统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/stats/productCategoryStats")
    public Result<List<ProductCategoryStatsVO>> getProductCategoryStats() {
        try {
            List<ProductCategoryStatsVO> stats = adminService.getProductCategoryStats();
            return Result.success("获取产品品类统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/stats/sixMonthStats")
    public Result<List<MonthlyStatsVO>> getSixMonthStats() {
        try {
            List<MonthlyStatsVO> stats = adminService.getSixMonthStats();
            return Result.success("获取近6个月统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/stats/dailyOrderStats")
    public Result<List<DailyOrderStatsVO>> getDailyOrderStats() {
        try {
            List<DailyOrderStatsVO> stats = adminService.getDailyOrderStatsForLast7Days();
            return Result.success("获取每日订单统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
