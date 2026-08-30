package org.dachuang_team.dc_backend_services.domain.PO.OrderPO;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_item_review",
        uniqueConstraints = {
                // 一个订单项只能评价一次
                @UniqueConstraint(name = "uk_review_order_item", columnNames = {"order_item_id"})
        }
)
public class OrderItemReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 订单号
    @Column(name = "order_number", nullable = false, length = 32, updatable = false)
    private String orderNumber;

    // 订单项ID
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private Long orderItemId;

    // 冗余保存，便于商品页/商家页直接查评价
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private Long sellerId;

    // 数据库必须保留真实买家，匿名只影响展示层
    @Column(name = "buyer_id", nullable = false, updatable = false)
    private Long buyerId;

    // 匿名标记：true=前台隐藏买家身份
    @Column(name = "anonymous", nullable = false)
    private Boolean anonymous = false;

    // 评分建议 1~5
    @Column(name = "rating", nullable = false, updatable = false)
    private Integer rating;

    // 评价内容
    @Column(name = "content", nullable = false, length = 1000, updatable = false)
    private String content;

    // 下单商品名快照，避免商品改名后评价上下文丢失
    @Column(name = "product_name_snapshot", length = 200, updatable = false)
    private String productNameSnapshot;

    // 只记录创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }
    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public Long getOrderItemId() {
        return orderItemId;
    }
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Long getSellerId() {
        return sellerId;
    }
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    public Long getBuyerId() {
        return buyerId;
    }
    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }
    public Boolean getAnonymous() {
        return anonymous;
    }
    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }
    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
