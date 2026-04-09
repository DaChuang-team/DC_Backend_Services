package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.UserAddressDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.UserUpdateDTO;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserAddress;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserPointsRecord;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.services.PointsRecordService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.dachuang_team.dc_backend_services.domain.DTO.UserDTO;


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

    @Autowired
    private PointsRecordService pointsRecordService;

    // 用户注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
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
                UserGeneral user = userService.getUserByUserName(userDTO.getUserName());
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", token);
                responseBody.put("userName", user.getUserName());
                responseBody.put("userPhone", user.getUserPhone());
                responseBody.put("userAvatarURL", user.getUserAvatarURL());
                responseBody.put("userGender", user.getUserGender());
                responseBody.put("userBirthday", user.getUserBirthday());
                responseBody.put("userStatus", user.getUserStatus());
                responseBody.put("userPoints", user.getPoints());
                responseBody.put("userPreference", user.getUserPreference());

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
    public Result<Map<String, Object>> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            // 1. 从安全上下文中获取 Filter 存入的 userId
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 2. 调用业务层更新信息
            userService.updateInfo(currentUserId, userUpdateDTO);

            // 3. 获取更新后的用户信息
            UserGeneral updatedUser = userService.getUserById(currentUserId);
            Map<String, Object> responseBody = buildUserResponse(updatedUser);

            return Result.success("更新成功", responseBody);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "用户信息更新失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage());
        }
    }

    // 用户签到接口
    @PostMapping("/checkIn")
    public Result<Integer> userCheckIn() {
        try {
            // 自动获取当前登录用户的 ID
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 执行签到
            userService.checkIn(currentUserId);

            // 获取最新积分返回给前端
            UserGeneral user = userService.getUserById(currentUserId);
            return Result.success("签到成功，获得 10 积分", user.getPoints()); //message 中说明获得了多少积分，data 中返回最新的积分总数

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    // 获取用户积分接口
    @GetMapping("/points")
    public Result<Integer> getUserPoints() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserGeneral user = userService.getUserById(currentUserId);
            return Result.success("获取积分成功", user.getPoints());
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    //获取用户积分变动记录接口，只返回近30天的记录，按照时间从晚到早排序
    @GetMapping("/points/records")
    public Result<List<UserPointsRecord>> getUserPointsRecords() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<UserPointsRecord> records = pointsRecordService.getPointsRecords(currentUserId);
            return Result.success("获取积分记录成功", records);
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    // 获取用户详细信息接口
    @GetMapping("/info")
    public Result<Map<String,Object>> getUserInfo() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
            UserGeneral user = userService.getUserById(currentUserId);
            Map<String, Object> responseBody = buildUserResponse(user);
            return Result.success("获取用户信息成功", responseBody);
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    //用户退出登录，强制使当前token过期
    @PostMapping("/logout")
    public Result<String> userLogout() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法退出登录");
            }
            if (userService.logout(currentUserId)) {
                return Result.success("退出登录成功", null);
            } else {
                return Result.error(400, "退出登录失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    // 获取该用户的所有地址
    @GetMapping("/address")
    public Result<List<Map<String, Object>>> getUserAddress() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法获取地址信息");
            }

            List<UserAddress> addresses = userService.getUserAddresses(currentUserId);
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (UserAddress address : addresses) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", address.getId());
                map.put("province", address.getProvince());
                map.put("city", address.getCity());
                map.put("district", address.getDistrict());
                map.put("detailAddress", address.getDetailAddress());
                map.put("receiverName", address.getReceiverName());
                map.put("receiverPhone", address.getReceiverPhone());
                map.put("remarks", address.getRemarks());
                map.put("isDefault", address.isDefault());
                resultList.add(map);
            }
            return Result.success("获取地址成功", resultList);

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    // 新增地址
    @PostMapping("/address")
    public Result<String> addUserAddress(@RequestBody UserAddressDTO addressDTO) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法添加地址");
            }
            userService.addUserAddress(currentUserId, addressDTO);
            return Result.success("地址添加成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    @PutMapping("/updateAddress")
    public Result<String> updateUserAddress(@RequestParam Long addressId, @RequestBody UserAddressDTO addressDTO) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法更新地址");
            }
            userService.updateUserAddress(currentUserId, addressId, addressDTO);
            return Result.success("地址更新成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteAddress")
    public Result<String> deleteUserAddress(@RequestParam Long addressId) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUserId == null) {
                return Result.error(401, "未认证，无法删除地址");
            }
            userService.deleteUserAddress(currentUserId, addressId);
            return Result.success("地址删除成功", null);

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    @PostMapping("/setDefaultAddress")
    public Result<String> setDefaultAddress(@RequestParam Long addressId) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法设置默认地址");
            }
            userService.setDefaultUserAddress(currentUserId, addressId);
            return Result.success("默认地址设置成功", null);

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    @PostMapping("/unsetDefaultAddress")
    public Result<String> unsetDefaultAddress(@RequestParam Long addressId) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法取消默认地址");
            }
            userService.unsetDefaultUserAddress(currentUserId, addressId);
            return Result.success("取消默认地址成功", null);

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }

    @GetMapping("/getDefaultAddress")
    public Result<Map<String,Object>> getDefaultAddress() {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证，无法获取默认地址");
            }
            UserAddress address = userService.getDefaultUserAddress(currentUserId);
            if(address == null) {
                return Result.error(200, "用户未设置默认地址", null);
            }
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", address.getId());
            responseBody.put("province", address.getProvince());
            responseBody.put("city", address.getCity());
            responseBody.put("district", address.getDistrict());
            responseBody.put("detailAddress", address.getDetailAddress());
            responseBody.put("receiverName", address.getReceiverName());
            responseBody.put("receiverPhone", address.getReceiverPhone());
            responseBody.put("remarks", address.getRemarks());
            return Result.success("获取默认地址成功", responseBody);

        } catch (IllegalArgumentException e) {
            return Result.error(402, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器开小差了: " + e.getMessage());
        }
    }


    /**
     * 获取所有用户信息接口
     * 参考 login 和 register 接口的实现风格
     */
    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllUsers() {
        // 1. 获取所有用户列表
        List<UserGeneral> userList = userService.getAllUsers();
        
        // 2. 准备返回的数据列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // 定义日期格式：年-月-日
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (UserGeneral user : userList) {
            Map<String, Object> userMap = new HashMap<>();
            
            // 映射字段 (按照 Navicat 中的字段名 and 用户要求)
            userMap.put("user_id", user.getUserId()); // 明确包含 userId
            userMap.put("user_name", user.getUserName());
            userMap.put("user_phone", user.getUserPhone());
            userMap.put("user_gender", user.getUserGender());
            userMap.put("points", user.getPoints());
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
    private Map<String, Object> buildUserResponse(UserGeneral user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", user.getUserName());
        map.put("userPhone", user.getUserPhone());
        map.put("userGender", user.getUserGender());
        map.put("userBirthday", user.getUserBirthday());
        map.put("userPreference", user.getUserPreference());
        map.put("userAvatarURL", user.getUserAvatarURL());
        map.put("userStatus", user.getUserStatus());
        map.put("points", user.getPoints());
        return map;
    }
}