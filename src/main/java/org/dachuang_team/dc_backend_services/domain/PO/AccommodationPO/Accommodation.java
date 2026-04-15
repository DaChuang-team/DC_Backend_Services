package org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "accommodation_name", nullable = false, length = 100)
    private String accommodationName;

    // 类型：HOTEL（酒店）/ HOSTEL（民宿）/ RESORT（度假村）
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "phone", length = 255)
    private String phone; // 联系方式，如电话号码或邮箱

    // 参考价格（起价）
    @Column(name = "price_from")
    private Double priceFrom;

    // 设施标签，JSON 数组存储
    @Column(name = "amenities", columnDefinition = "JSON")
    private String amenities;

    @Column(name = "star_rating")
    private Integer starRating; // 星级，1~5

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;


    @Column(name = "check_in_time", length = 10)
    private String checkInTime; // 如 "14:00"

    @Column(name = "check_out_time", length = 10)
    private String checkOutTime; // 如 "12:00"

    @Column(name = "policy_note", length = 500)
    private String policyNote; // 入住须知、取消政策等

    // 首图缩略图 URL，冗余存储方便列表页展示
    @Column(name = "tb_image_url", length = 255)
    private String tbImageUrl;

    // 审核状态，默认 false 表示未审核通过
    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    // 外键：指向商家表，表示该住宿由哪个商家发布
    @ManyToOne
    @JoinColumn(name = "seller", nullable = false, updatable = false)
    private Merchant seller;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private Long sellerId;

    public String getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
    public Long getAccommodationId() {
        return accommodationId;
    }
    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
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
    public Boolean getApproved() {
        return approved;
    }
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
    public Merchant getSeller() {
        return seller;
    }
    public void setSeller(Merchant seller) {
        this.seller = seller;
    }
    public Long getSellerId() {
        return sellerId;
    }
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
