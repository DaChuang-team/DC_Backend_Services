package org.dachuang_team.dc_backend_services.domain.PO.UserPO;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_general")
public class UserGeneral {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 用户自定义昵称，唯一且非空，长度限制45
    @Column(name = "user_name", nullable = false, length = 45 , unique = true)
    private String userName;
    // 用户手机号，用户登录
    @Column(name = "user_phone", nullable = false, length = 13, unique = true)
    private String userPhone;
    @Column(name = "user_password", nullable = false, length = 62)
    private String userPassword;
    @Column(name = "user_preference", length = 100)
    private String userPreference;
    @Column(name = "user_gender")
    private char userGender; //规则: 'M'-男, 'F'-女, 'U'-未知
    @Column(name = "user_avatar_url", length = 200)
    private String userAvatarURL;
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "last_login")
    private LocalDateTime lastLoginAt;// 最后一次登录时间，可以为 null，（每次登录时更新）
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;// 更新时间（每次修改自动更新）
    @Column(name = "user_birthday")
    private String userBirthday; //用户生日，格式YYYY-MM-DD

    @Column(name = "user_status", nullable = false, length = 10)
    private String userStatus = "正常"; // 用户状态：'正常', '异常'

    @Column(name = "points", columnDefinition = "INT DEFAULT 0") // 用户积分，初始为0，通过签到可以增加积分，积分可以用于AI个性化推荐请求
    private Integer points;

    // Getters and Setters
    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserPreference() {
        return userPreference;
    }

    public void setUserPreference(String userPreference) {
        this.userPreference = userPreference;
    }

    public char getUserGender() {
        return userGender;
    }

    public void setUserGender(char userGender) {
        this.userGender = userGender;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "User_General{" +
                "userPhone='" + userPhone + '\'' +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }
}
