package org.dachuang_team.dc_backend_services.domain.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class UserDTO {
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("userPhone")
    private String userPhone;
    @JsonProperty("userPassword")
    @NotBlank(message = "密码不能为空")
    private String userPassword;
    @JsonProperty("userGender")
    private Character userGender;

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

    public Character getUserGender() {
        return userGender;
    }

    public void setUserGender(Character userGender) {
        this.userGender = userGender;
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