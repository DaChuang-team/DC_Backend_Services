package org.dachuang_team.dc_backend_services.domain.VO;

import org.dachuang_team.dc_backend_services.domain.PO.OrderItem;

import java.math.BigDecimal;

public class OrderItemVO {

    private String itemId;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public static OrderItemVO from(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.itemId = item.getId();
        vo.productId = item.getProductId();
        vo.productName = item.getProductName();
        vo.unitPrice = item.getUnitPrice();
        vo.quantity = item.getQuantity();
        vo.subtotal = item.getSubtotal();
        return vo;
    }

    public String getItemId() { return itemId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
