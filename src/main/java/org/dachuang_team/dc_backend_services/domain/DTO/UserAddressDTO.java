package org.dachuang_team.dc_backend_services.domain.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class UserAddressDTO {
    @NotBlank(message = "收件人姓名不能为空")
    private String receiverName;
    @NotBlank(message = "收件人手机号不能为空")
    private String receiverPhone;
    @NotBlank(message = "省份不能为空")
    private String province;
    @NotBlank(message = "城市不能为空")
    private String city;
    @NotBlank(message = "区县不能为空")
    private String district;
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    @NotBlank(message = "备注不能为空")
    private String remarks;
    @JsonProperty("isDefault")
    @NotBlank(message = "是否默认地址不能为空")
    private Boolean isDefault;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}
