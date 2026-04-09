package org.dachuang_team.dc_backend_services.domain.PO;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 对应数据库中的 admin 表
 * 使用 JPA 注解自动映射数据库字段
 */
@Entity
@Table(name = "admin") // 指定数据库表名为 admin
public class Admin {

    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键自增
    @Column(name = "admin_id", nullable = false) // 对应数据库字段 admin_id
    private Long adminId;

    @Column(name = "admin_name", nullable = false, length = 45, unique = true) // 用户名，不允许为空，长度45，唯一
    private String adminName;

    @Column(name = "admin_password", nullable = false, length = 62) // 登录密码，不允许为空
    private String adminPassword;

    @Column(name = "admin_role", nullable = false, length = 20) // 角色（权限）：ADMIN / SUPER_ADMIN，由传入的邀请码区分
    private String adminRole;

    @Column(name = "last_login") // 最近登录时间
    private LocalDateTime lastLogin;

    // --- Getters and Setters ---

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", adminRole='" + adminRole + '\'' +
                '}';
    }
}
