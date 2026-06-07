package org.dachuang_team.dc_backend_services.domain.VO;

import java.math.BigDecimal;

public class DailyOrderStatsVO {
    private String date;
    private Long orderCount;
    private BigDecimal totalSales;

    public DailyOrderStatsVO(String date, Long orderCount, BigDecimal totalSales) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalSales = totalSales;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
