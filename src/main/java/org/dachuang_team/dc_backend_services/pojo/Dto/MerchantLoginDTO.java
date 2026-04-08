package org.dachuang_team.dc_backend_services.pojo.Dto;

import jakarta.validation.constraints.NotBlank;

public class MerchantLoginDTO {
    @NotBlank(message = "商户登录账号不能为空")
    private String loginID;
    @NotBlank(message = "商户登录密码不能为空")
    private String password;

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
