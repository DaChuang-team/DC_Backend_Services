package org.dachuang_team.dc_backend_services.domain.PO;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    private String id;

    //订单项关联订单，单向多对一关系。
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    //商品id
    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    //商品名称快照
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    //商品完整快照
    @Column(name = "product_snapshot", columnDefinition = "TEXT")
    private String productSnapshot;

    //单价快照
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    //下单数量，最小为1
    @Column(nullable = false)
    private Integer quantity;

    //商品小计，单价 * 数量，订单项创建时计算并保存，订单项不可变
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    //订单项创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OrderItem() {}

    // 强制校验
    public OrderItem(String productId, String productName,
                     String productSnapshot, BigDecimal unitPrice, Integer quantity) {
        if (productId == null || productId.isBlank())
            throw new IllegalArgumentException("productId 不能为空");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("productName 不能为空");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("单价必须大于 0");
        if (quantity == null || quantity < 1)
            throw new IllegalArgumentException("数量必须 ≥ 1");

        this.id = UUID.randomUUID().toString().replace("-", "");
        this.productId = productId;
        this.productName = productName;
        this.productSnapshot = productSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }  // 仅供 Order.setItems() 内部调用
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductSnapshot() { return productSnapshot; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}