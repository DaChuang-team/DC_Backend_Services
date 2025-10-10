package org.dachuang_team.dc_backend_services.pojo.Dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("userPhone")
    private String userPhone;
    @JsonProperty("userPassword")
    private String userPassword;

    // Getters and Setters
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

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }
}