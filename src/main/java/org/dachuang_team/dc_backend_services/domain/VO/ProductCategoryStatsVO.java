package org.dachuang_team.dc_backend_services.domain.VO;

public class ProductCategoryStatsVO {
    private Integer category;
    private Long count;

    public ProductCategoryStatsVO(Integer category, Long count) {
        this.category = category;
        this.count = count;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
