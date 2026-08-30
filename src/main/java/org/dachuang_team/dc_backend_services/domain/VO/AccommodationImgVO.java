package org.dachuang_team.dc_backend_services.domain.VO;

public class AccommodationImgVO {
    private Long id;
    private String url;
    private String thumbnailUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
    private Long accommodationId;

    public Integer getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public Boolean getPrimary() {
        return isPrimary;
    }
    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }
    public Long getAccommodationId() {
        return accommodationId;
    }
    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }
}
