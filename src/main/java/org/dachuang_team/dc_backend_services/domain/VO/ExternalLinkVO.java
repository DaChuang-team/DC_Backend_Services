package org.dachuang_team.dc_backend_services.domain.VO;

public class ExternalLinkVO {
    private Long id;
    private String platform;
    private String url;
    private Boolean topped;
    private Boolean approved;

    public Boolean getTopped() {
        return topped;
    }
    public void setTopped(Boolean topped) {
        this.topped = topped;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Boolean getApproved() {
        return approved;
    }
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
