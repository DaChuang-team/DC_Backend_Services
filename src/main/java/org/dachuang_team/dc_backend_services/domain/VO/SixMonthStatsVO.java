package org.dachuang_team.dc_backend_services.domain.VO;

public class SixMonthStatsVO {
    private Long newUsers;
    private Long newOrders;
    private Long newProducts;

    public SixMonthStatsVO(Long newUsers, Long newOrders, Long newProducts) {
        this.newUsers = newUsers;
        this.newOrders = newOrders;
        this.newProducts = newProducts;
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

    public Long getNewProducts() {
        return newProducts;
    }

    public void setNewProducts(Long newProducts) {
        this.newProducts = newProducts;
    }
}
