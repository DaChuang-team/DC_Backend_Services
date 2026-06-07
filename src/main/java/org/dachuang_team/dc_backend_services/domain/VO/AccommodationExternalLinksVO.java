package org.dachuang_team.dc_backend_services.domain.VO;

import java.util.List;

public class AccommodationExternalLinksVO {
    private String accommodationId;
    private String accommodationName;
    private String type;
    private String address;
    private List<ExternalLinkVO> externalLinks;

    public String getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(String accommodationId) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ExternalLinkVO> getExternalLinks() {
        return externalLinks;
    }

    public void setExternalLinks(List<ExternalLinkVO> externalLinks) {
        this.externalLinks = externalLinks;
    }
}
