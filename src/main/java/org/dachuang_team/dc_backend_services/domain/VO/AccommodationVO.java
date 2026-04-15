package org.dachuang_team.dc_backend_services.domain.VO;

import java.util.List;

public class AccommodationVO {
    private String accommodationName;
    private String phone;
    private String type; //HOTEL（酒店）/ HOSTEL（民宿）/ RESORT（度假村）
    private String description;
    private Double priceFrom;
    private String amenities;
    private Integer starRating; // 星级，1~5
    private String address;
    private Double latitude;
    private Double longitude;
    private String checkInTime; // 如 "14:00"
    private String checkOutTime; // 如 "12:00"
    private String policyNote; // 入住须知、取消政策等
    private String tbImageUrl; // 首图缩略图 URL
    private List<AccommodationImgVO> images; // 酒店图片列表

    public String getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
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
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getCheckOutTime() {
        return checkOutTime;
    }
    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
    public String getPolicyNote() {
        return policyNote;
    }
    public void setPolicyNote(String policyNote) {
        this.policyNote = policyNote;
    }
    public String getTbImageUrl() {
        return tbImageUrl;
    }
    public void setTbImageUrl(String tbImageUrl) {
        this.tbImageUrl = tbImageUrl;
    }
    public List<AccommodationImgVO> getImages() {
        return images;
    }
    public void setImages(List<AccommodationImgVO> images) {
        this.images = images;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
