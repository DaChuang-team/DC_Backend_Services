package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

@Entity
@Table(name = "sysImage")
public class sysImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "image_Name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "purpose" , nullable = false, length = 25)
    private String purpose; // MAIN_PAGE_BANNER, PRODUCT_PAGE_BANNER, USER_DEFAULT_AVATAR etc.

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}