package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.ProductPO.ProductOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItem, Long> {
    // 按订单ID查询商品订单项
    List<ProductOrderItem> findByOrderOrderId(Long orderId);
}
