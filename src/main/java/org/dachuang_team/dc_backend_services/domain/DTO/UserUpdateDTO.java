package org.dachuang_team.dc_backend_services.domain.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;


public class UserUpdateDTO {
    @JsonProperty("newName")//前端传入的新用户名
    private String userName;
    @JsonProperty("userPreference")
    private String userPreference;
    @JsonProperty("userGender")
    private Character userGender;
    @JsonProperty("userAvatarURL")
    private String userAvatarURL;
    @JsonProperty("userBirthday")
    private String userBirthday;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
}
