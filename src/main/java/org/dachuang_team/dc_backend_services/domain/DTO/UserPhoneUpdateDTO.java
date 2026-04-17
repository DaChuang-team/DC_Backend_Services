package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

public class UserPhoneUpdateDTO {
    @NotBlank(message = "旧手机号验证码不能为空")
    private String oldPhoneVerifyCode;
    @NotBlank(message = "新手机号不能为空")
    private String newPhone;
    @NotBlank(message = "新手机号验证码不能为空")
    private String newPhoneVerifyCode;

    public String getOldPhoneVerifyCode() {
        return oldPhoneVerifyCode;
    }
    public void setOldPhoneVerifyCode(String oldPhoneVerifyCode) {
        this.oldPhoneVerifyCode = oldPhoneVerifyCode;
    }
    public String getNewPhone() {
        return newPhone;
    }
    public void setNewPhone(String newPhone) {
        this.newPhone = newPhone;
    }
    public String getNewPhoneVerifyCode() {
        return newPhoneVerifyCode;
    }
    public void setNewPhoneVerifyCode(String newPhoneVerifyCode) {
        this.newPhoneVerifyCode = newPhoneVerifyCode;
    }
}
