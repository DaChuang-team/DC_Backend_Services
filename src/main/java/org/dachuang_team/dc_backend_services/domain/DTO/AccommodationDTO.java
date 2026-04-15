package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public class AccommodationDTO {
    @NotBlank(message = "酒店名称不能为空")
    private String accommodationName;
    @NotBlank(message = "酒店联系电话不能为空")
    private String phone;
    @NotBlank(message = "酒店类型不能为空")
    private String type; //HOTEL（酒店）/ HOSTEL（民宿）/ RESORT（度假村）
    private String description;
    private Double priceFrom;
    private String amenities;
    private Integer starRating; // 星级，1~5
    @NotBlank(message = "酒店地址不能为空")
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String checkInTime; // 如 "14:00"
    private String checkOutTime; // 如 "12:00"
    private String policyNote; // 入住须知、取消政策等
    private List<Long> imageIds;

    public String getPolicyNote() {
        return policyNote;
    }
    public void setPolicyNote(String policyNote) {
        this.policyNote = policyNote;
    }
    public String getAccommodationName() {
        return accommodationName;
    }
    public void setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Double getPriceFrom() {
        return priceFrom;
    }
    public void setPriceFrom(Double priceFrom) {
        this.priceFrom = priceFrom;
    }
    public String getAmenities() {
        return amenities;
    }
    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
    public Integer getStarRating() {
        return starRating;
    }
    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public BigDecimal getLatitude() {
        return latitude;
    }
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    public BigDecimal getLongitude() {
        return longitude;
    }
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    public String getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
    public String getCheckOutTime() {
        return checkOutTime;
    }
    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public List<Long> getImageIds() {
        return imageIds;
    }
    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
