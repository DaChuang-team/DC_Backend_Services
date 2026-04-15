package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

public class MerchantRegisterDTO {

    @NotBlank(message = "商家姓名不能为空")
    private String merchantName;
    private String shopName;
    @NotBlank(message = "商户联系电话不能为空")
    private String merchantPhone;
    @NotBlank(message = "商户登录账号不能为空")
    private String loginID;
    @NotBlank(message = "商户登录密码不能为空")
    private String password;
    private String bannerUrl;
    private String description;
    @NotBlank(message = "商户地址不能为空")
    private String merchantAddress;

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getMerchantName() {
        return merchantName;
    }
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
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
    public String getBannerUrl() {
        return bannerUrl;
    }
    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }
    public String getMerchantAddress() {
        return merchantAddress;
    }
    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }
    public String getMerchantPhone() {
        return merchantPhone;
    }
    public void setMerchantPhone(String merchantPhone) {
        this.merchantPhone = merchantPhone;
    }
    public String getShopName() {
        return shopName;
    }
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
