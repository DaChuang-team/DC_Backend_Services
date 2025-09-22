package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "userGeneral")
public class User_General {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_id", nullable = false)
    private int UserID;

    @Column(name = "User_name", nullable = false, length = 45 , unique = true)
    private String UserName;

    @Column(name = "User_phone", nullable = false, length = 13, unique = true)
    private String UserPhone;

    @Column(name = "User_password", nullable = false, length = 20)
    private String UserPassword;

    @Column(name = "User_Preference", length = 100)
    private String UserPreference;

    @Column(name = "User_Gender")
    private char UserGender; //规则: 'M'-男, 'F'-女, 'U'-未知

    @Column(name = "User_Avatar_URL", length = 200)
    private String UserAvatarURL;

    @Column(name = "Create_Time", length = 20)
    private LocalDateTime CreateTime;

    // 最后一次登录时间，可以为 null
    @Column(name = "Last_Login")
    private LocalDateTime lastLoginAt;

    // 更新时间（每次修改自动更新）
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "User_Birthday")
    private LocalDateTime UserBirthday;

//    @Column(name = "User_Level")
//    private int UserLevel = 1; //或许后续可以推出用户积分和等级系统？
//
//    @Column(name = "User_Points")
//    private int UserPoints = 0; //用户积分，初始为0

    @Column(name = "User_Permissions")
    private int UserPermissions = 0;  //ps:本来还打算加个UserStatus的，但是想想直接在Permissions里设置就好了：0-普通用户，1-管理员，-1-封禁用户



    // Getters and Setters

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getUserPassword() {
        return UserPassword;
    }

    public void setUserPassword(String userPassword) {
        UserPassword = userPassword;
    }

    public String getUserPreference() {
        return UserPreference;
    }

    public void setUserPreference(String userPreference) {
        UserPreference = userPreference;
    }

    public char isUserGender() {
        return UserGender;
    }

    public void setUserGender(char userGender) {
        UserGender = userGender;
    }

    public String getUserAvatarURL() {
        return UserAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        UserAvatarURL = userAvatarURL;
    }

    public LocalDateTime getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        CreateTime = createTime;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getUserBirthday() {
        return UserBirthday;
    }

    public void setUserBirthday(LocalDateTime userBirthday) {
        UserBirthday = userBirthday;
    }

    public int getUserPermissions() {
        return UserPermissions;
    }

    public void setUserPermissions(int userPermissions) {
        UserPermissions = userPermissions;
    }
}
