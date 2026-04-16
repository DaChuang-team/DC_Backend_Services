package org.dachuang_team.dc_backend_services.domain.DTO;

public class ExternalLinkDTO {
    private String platform;
    private String url;

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
}
