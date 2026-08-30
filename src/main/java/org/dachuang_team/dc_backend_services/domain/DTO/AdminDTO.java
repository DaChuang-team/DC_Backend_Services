package org.dachuang_team.dc_backend_services.domain.DTO;

import java.time.LocalDateTime;

/**
 * 管理员数据传输对象 (DTO)
 * 用于在前端与后端之间传输管理员信息，保护数据库实体结构
 */
public class AdminDTO {

    private Long adminId;         // 管理员ID
    private String adminName;     // 管理员用户名
    private String adminPassword; // 管理员密码
    private String adminRole;     // 管理员角色
    private LocalDateTime lastLogin; // 最后登录时间
    private String inviteCode;    // 邀请码
    private Integer status;       // 状态：0-正常，1-禁用
    private String newPassword;   // 新密码（超级管理员重置密码时使用）

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

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "AdminDTO{" +
                "adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", adminRole='" + adminRole + '\'' +
                ", lastLogin=" + lastLogin + '\'' +
                ", inviteCode='" + inviteCode +
                '}';
    }
}
