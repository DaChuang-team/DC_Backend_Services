package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

@Entity
@Table(name = "product_order_item")
public class ProductOrderItem {
    // 商品订单项，保存商品类订单的快照信息
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // 购买数量
    @Column(nullable = false)
    private Integer quantity;

    // 下单时的总金额
    @Column(nullable = false)
    private Double priceAtOrder;

    // 下单时的商品名称快照
    @Column(length = 100)
    private String itemNameSnapshot;

    // 下单时的单价快照
    private Double unitPriceSnapshot;

    // 所属订单
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // 对应商品
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public String getItemNameSnapshot() {
        return itemNameSnapshot;
    }

    public void setItemNameSnapshot(String itemNameSnapshot) {
        this.itemNameSnapshot = itemNameSnapshot;
    }

    public Double getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(Double unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
