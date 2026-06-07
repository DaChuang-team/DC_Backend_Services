package org.dachuang_team.dc_backend_services.domain.VO;

import java.math.BigDecimal;

public class OrderStatsVO {
    private Long orderCount;
    private BigDecimal totalSales;

    public OrderStatsVO(Long orderCount, BigDecimal totalSales) {
        this.orderCount = orderCount;
        this.totalSales = totalSales;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
