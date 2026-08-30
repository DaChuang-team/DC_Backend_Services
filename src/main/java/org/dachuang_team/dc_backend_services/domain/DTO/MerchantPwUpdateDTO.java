package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

public class MerchantPwUpdateDTO {
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
    private String code;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
