package org.dachuang_team.dc_backend_services.domain.VO;

import java.math.BigDecimal;

public class MonthlyStatsVO {
    private String month;
    private Long newUsers;
    private Long newOrders;
    private BigDecimal totalSales;
    private Long newProducts;

    public MonthlyStatsVO(String month, Long newUsers, Long newOrders, BigDecimal totalSales, Long newProducts) {
        this.month = month;
        this.newUsers = newUsers;
        this.newOrders = newOrders;
        this.totalSales = totalSales;
        this.newProducts = newProducts;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Long newUsers) {
        this.newUsers = newUsers;
    }

    public Long getNewOrders() {
        return newOrders;
    }

    public void setNewOrders(Long newOrders) {
        this.newOrders = newOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Long getNewProducts() {
        return newProducts;
    }

    public void setNewProducts(Long newProducts) {
        this.newProducts = newProducts;
    }
}
