package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateOrderRequestDTO {
    @NotBlank(message = "收货地址不能为空")
    private String address;

    private List<OrderItemDto> items;

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static class OrderItemDto {
        @NotBlank(message = "商品ID不能为空")
        private String productId;
        @NotBlank(message = "商品名称不能为空")
        private String productName;
        @NotBlank(message = "数量不能为空")
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

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }
    }
}
