package org.dachuang_team.dc_backend_services.domain.DTO;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequestDTO {
    private String buyerId;
    private String sellerId;
    private List<OrderItemDto> items;

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }


    public static class OrderItemDto {
        private String productId;
        private String productName;
        private String productSnapshot;
        private BigDecimal unitPrice;
        private Integer quantity;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductSnapshot() {
            return productSnapshot;
        }

        public void setProductSnapshot(String productSnapshot) {
            this.productSnapshot = productSnapshot;
        }
    }
}
