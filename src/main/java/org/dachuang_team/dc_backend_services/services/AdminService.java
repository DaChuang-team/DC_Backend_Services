package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.dachuang_team.dc_backend_services.pojo.Admin;
import org.dachuang_team.dc_backend_services.pojo.Dto.AdminDTO;
import org.dachuang_team.dc_backend_services.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 * 处理管理员注册的具体逻辑，包括用户名检查和密码加密
 */
@Service
public class AdminService implements IAdminService {

    @Autowired
    private AdminRepository adminRepository;

    // 密码加密器，用于对管理员密码进行加密存储
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String NROMAL_ADMIN_INVITE_CODE = "$2a$10$RKiUjcBWpgkdfAt9XtqWe.B2PgUZ5ouKgFz7a0EHE.uD5xfp6qI7m";
    // 这里的哈希值是对预设邀请码进行 BCrypt 加密后的结果，普通管理员静态邀请码：xczlAdmin

    private static final String SUPER_ADMIN_INVITE_CODE = "$2a$10$ssaGpWklAl8Z1VtcMNc/mun.MwZA2QngfhhyNBDtfISe9zQRFL9CC";
    // 超级管理员静态邀请码：xczlAdminPro

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    // 生成并存储Token
    AuthService authService;

    /**
     * 注册管理员逻辑
     */
    @Override
    public void registerAdmin(AdminDTO adminDTO) {

        String adminRole;
        if (adminDTO.getInviteCode() != null && passwordEncoder.matches(adminDTO.getInviteCode(), NROMAL_ADMIN_INVITE_CODE)) {
            adminRole = "ADMIN"; // 普通管理员
        } else if (adminDTO.getInviteCode() != null && passwordEncoder.matches(adminDTO.getInviteCode(), SUPER_ADMIN_INVITE_CODE)) {
            adminRole = "SUPER_ADMIN"; // 超级管理员
        } else {
            throw new IllegalArgumentException("无效的邀请码");
        }

        // 1. 检查用户名是否已存在
        if (adminRepository.findByAdminName(adminDTO.getAdminName()) != null) {
            throw new IllegalArgumentException("管理员名称: " + adminDTO.getAdminName() + " 已存在");
        }

        // 2. 创建管理员实体对象并填充数据
        Admin newAdmin = new Admin();
        newAdmin.setAdminName(adminDTO.getAdminName());
        
        // 3. 对密码进行加密处理
        String encodedPassword = passwordEncoder.encode(adminDTO.getAdminPassword());
        newAdmin.setAdminPassword(encodedPassword);
        
        // 4. 设置管理员角色
        newAdmin.setAdminRole(adminRole);

        // 5. 保存到数据库
        adminRepository.save(newAdmin);
    }

    /**
     * 验证管理员登录逻辑
     */
    @Override
    @Transactional
    public String authenticateAdmin(String adminName, String rawPassword) {
        try{
            // 1. 根据用户名查找管理员
            Admin admin = adminRepository.findByAdminName(adminName);

            // 基础校验
            if (admin == null || !passwordEncoder.matches(rawPassword, admin.getAdminPassword())) {
                logger.warn("用户名或密码错误: {}", adminName);
                throw new IllegalArgumentException("用户名或密码错误");
            }

            // 更新最后登录时间
            LocalDateTime now = LocalDateTime.now();
            admin.setLastLogin(now);
            adminRepository.save(admin);
            logger.info("管理员最后登录时间已更新: {}", now);


            String token = authService.generateToken(admin.getAdminId(), admin.getAdminRole());
            logger.info("Token 生成成功: {}", token);

            return token;
        } catch (IllegalArgumentException e) {
            logger.error("登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("服务器错误: {}", e.getMessage(), e);
            throw new RuntimeException("登录时发生服务器错误");
        }
    }

    /**
     * 获取管理员信息
     */
    @Override
    public AdminDTO getAdminByAdminName(String adminName) {
        Admin admin = adminRepository.findByAdminName(adminName);
        if (admin == null) {
            return null;
        }
        AdminDTO dto = new AdminDTO();
        dto.setAdminName(admin.getAdminName());
        dto.setAdminRole(admin.getAdminRole());
        // 不返回密码字段
        return dto;
    }

    /**
     * 获取所有管理员信息
     * 将数据库实体对象转换为 DTO 对象列表，保护敏感数据（如密码）
     */
    @Override
    public List<AdminDTO> getAllAdmins() {
        // 1. 获取数据库中所有管理员实体
        List<Admin> adminList = adminRepository.findAll();
        
        // 2. 将实体列表转换为 DTO 列表返回
        return adminList.stream().map(admin -> {
            AdminDTO dto = new AdminDTO();
            dto.setAdminName(admin.getAdminName());
            dto.setAdminRole(admin.getAdminRole());
            // 设置管理员ID，方便前端管理
            dto.setAdminId(admin.getAdminId());
            // 设置最后登录时间
            dto.setLastLogin(admin.getLastLogin());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteAdmin(String targetAdminName, String currentAdminName, String currentAdminPassword) {
        // 获取当前操作的管理员
        Admin currentAdmin = adminRepository.findByAdminName(currentAdminName);

        // 验证当前管理员身份和密码
        if (currentAdmin == null || !passwordEncoder.matches(currentAdminPassword, currentAdmin.getAdminPassword())) {
            throw new IllegalArgumentException("当前管理员身份验证失败");
        }

        if (!"SUPER_ADMIN".equals(currentAdmin.getAdminRole())) {
            throw new IllegalArgumentException("只有超级管理员可以删除管理员");
        }

        // 查找目标管理员
        Admin admin = adminRepository.findByAdminName(targetAdminName);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在: " + targetAdminName);
        }

        // 防止超级管理员删除自己
        if (targetAdminName.equals(currentAdminName)) {
            throw new IllegalArgumentException("超级管理员不能删除自己");
        }

        // 删除管理员
        adminRepository.delete(admin);
        return true;
    }
}
