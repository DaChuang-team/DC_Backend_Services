package org.dachuang_team.dc_backend_services.pojo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserUpdateDTO {
    @JsonProperty("newName")//前端传入的新用户名
    private String userName;
    @JsonProperty("userPhone")
    private String userPhone;
    @JsonProperty("userPassword")
    private String userPassword;
    @JsonProperty("oldPassword")// 仅在修改密码或手机号时需要提供旧密码以验证身份
    private String oldPassword;
    @JsonProperty("userPreference")
    private String userPreference;
    @JsonProperty("userGender")
    private Character userGender;
    @JsonProperty("userAvatarURL")
    private String userAvatarURL;
    @JsonProperty("userBirthday")
    private String userBirthday;
    @JsonProperty("oldPhone")// 修改手机号时需要提供原手机号进行验证
    private String oldPhone;
    @JsonProperty("userStatus")// 用户状态
    private String userStatus;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getOldPhone() {
        return oldPhone;
    }

    public void setOldPhone(String oldPhone) {
        this.oldPhone = oldPhone;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
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

    public Character getUserGender() {
        return userGender;
    }

    public void setUserGender(Character userGender) {
        this.userGender = userGender;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }

    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }


    @Override
    public String toString() {
        return "userUpdateDTO{" +
                "userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", oldPassword='" + oldPassword + '\'' +
                ", userPreference='" + userPreference + '\'' +
                ", userGender=" + userGender +
                ", userAvatarURL='" + userAvatarURL + '\'' +
                ", userBirthday='" + userBirthday + '\'' +
                ", oldPhone='" + oldPhone + '\'' +
                ", userStatus='" + userStatus + '\'' +
                '}';
    }
}
