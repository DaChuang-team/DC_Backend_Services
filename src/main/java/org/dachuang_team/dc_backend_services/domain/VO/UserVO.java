package org.dachuang_team.dc_backend_services.domain.VO;

public class UserVO {
    private String userName;
    private String userPhone;
    private char userGender;
    private String userPreference;
    private String userAvatarURL;
    private String userBirthday;
    private String userStatus;
    private Integer points;

    public String getUserStatus() {
        return userStatus;
    }
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
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
    public String getUserPreference() {
        return userPreference;
    }
    public void setUserPreference(String userPreference) {
        this.userPreference = userPreference;
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
    public Integer getPoints() {
        return points;
    }
    public void setPoints(Integer points) {
        this.points = points;
    }
    public char getUserGender() {
        return userGender;
    }
    public void setUserGender(char userGender) {
        this.userGender = userGender;
    }
}
