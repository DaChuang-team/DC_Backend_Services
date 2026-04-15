package org.dachuang_team.dc_backend_services.domain.VO;


public class MerchantVO {

    private String merchantName;
    private String shopName;
    private String merchantPhone;
    private String loginID;
    private String bannerUrl;
    private String description;
    private String merchantAddress;
    private Integer status;
    private String token;

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
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getShopName() {
        return shopName;
    }
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
