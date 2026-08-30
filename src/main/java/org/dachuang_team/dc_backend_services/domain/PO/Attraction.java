package org.dachuang_team.dc_backend_services.domain.PO;

import jakarta.persistence.*;

@Entity
@Table(name = "attraction")
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attraction_id", nullable = false)
    private Long attractionId;

    @Column(name = "attraction_name", nullable = false, length = 30)
    private String attractionName;
    @Column(name = "attraction_description", length = 200)
    private String attractionDescription;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "attraction_image_URL", length = 200)
    private String attractionImageURL;
    @Column(name = "attraction_tag", length = 50)
    private String attractionTag;
    @Column(name = "attraction_rating")
    private Double attractionRating;
    @Column(name = "attraction_region", length = 20)
    private String attractionRegion;

    public Double getAttractionRating() {
        return attractionRating;
    }

    public void setAttractionRating(Double attractionRating) {
        this.attractionRating = attractionRating;
    }

    public Long getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(Long attractionId) {
        this.attractionId = attractionId;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public String getAttractionDescription() {
        return attractionDescription;
    }

    public void setAttractionDescription(String attractionDescription) {
        this.attractionDescription = attractionDescription;
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

    public String getAttractionImageURL() {
        return attractionImageURL;
    }

    public void setAttractionImageURL(String attractionImageURL) {
        this.attractionImageURL = attractionImageURL;
    }

    public String getAttractionTag() {
        return attractionTag;
    }

    public void setAttractionTag(String attractionTag) {
        this.attractionTag = attractionTag;
    }

    public String getAttractionRegion() {
        return attractionRegion;
    }

    public void setAttractionRegion(String attractionRegion) {
        this.attractionRegion = attractionRegion;
    }
}