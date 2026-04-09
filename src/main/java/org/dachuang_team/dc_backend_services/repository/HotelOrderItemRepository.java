package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.HotelOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelOrderItemRepository extends JpaRepository<HotelOrderItem, Long> {
    // 按订单ID查询酒店订单项
    List<HotelOrderItem> findByOrderOrderId(Long orderId);
}
