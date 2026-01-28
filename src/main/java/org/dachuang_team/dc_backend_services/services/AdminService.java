package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Admin;
import org.dachuang_team.dc_backend_services.pojo.Dto.AdminDTO;
import org.dachuang_team.dc_backend_services.repository.AdminRepository;
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
public class AdminService implements IAdminServices {

    @Autowired
    private AdminRepository adminRepository;

    // 密码加密器，用于对管理员密码进行加密存储
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册管理员逻辑
     */
    @Override
    public void registerAdmin(AdminDTO adminDTO) {
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
        newAdmin.setAdminRole(adminDTO.getAdminRole());

        // 5. 保存到数据库
        adminRepository.save(newAdmin);
    }

    /**
     * 验证管理员登录逻辑
     */
    @Override
    public boolean authenticateAdmin(String adminName, String rawPassword) {
        // 1. 根据用户名查找管理员
        Admin admin = adminRepository.findByAdminName(adminName);
        
        // 2. 如果管理员存在，并且加密后的密码匹配
        if (admin != null && passwordEncoder.matches(rawPassword, admin.getAdminPassword())) {
            // 3. 登录成功，更新最后登录时间
            admin.setLastLogin(LocalDateTime.now());
            adminRepository.save(admin); // 保存更新时间到数据库
            return true;
        }
        
        // 4. 验证失败
        return false;
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
    public boolean deleteAdmin(String adminName) {
        // 1. 查找管理员是否存在
        Admin admin = adminRepository.findByAdminName(adminName);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在: " + adminName);
        }
        
        // 2. 执行删除
        adminRepository.delete(admin);
        return true;
    }
}
