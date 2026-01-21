package org.dachuang_team.dc_backend_services.pojo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;


public class userUpdateDTO {
    @JsonProperty("newName")//前端传入的新用户名,可选，用于验证的用户名通过Query参数传入
    private String userName;
    @JsonProperty("userPhone")
    private String userPhone;
    @JsonProperty("newPassword")
    private String userPassword;
    @NotNull
    @JsonProperty("oldPassword")//为保证安全，修改信息时需要提供旧密码以验证身份
    private String oldPassword;
    @JsonProperty("userPreference")
    private String userPreference;
    @JsonProperty("userGender")
    private Character userGender;
    @JsonProperty("userAvatarURL")
    private String userAvatarURL;
    @JsonProperty("userBirthday")
    private String userBirthday;
    @NotNull
    @JsonProperty("NPC")//为保证安全，需要前端发送是否需要修改密码的标志，如果无需修改密码，则将原密码传回后端通过oldPassword验证即可
    private boolean needPasswordChange = false;

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

    public boolean isNeedPasswordChange() {
        return needPasswordChange;
    }

    public void setNeedPasswordChange(boolean needPasswordChange) {
        this.needPasswordChange = needPasswordChange;
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
                ", userBirthday=" + userBirthday +
                ", needPasswordChange=" + needPasswordChange +
                '}';
    }
}
