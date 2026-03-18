package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // 业务订单号，用于前后端和业务系统跟踪
    @Column(nullable = false, unique = true, length = 64)
    private String orderNo;

    // 订单原始总金额
    private Double totalPrice;
    // 用户实际支付金额
    private Double payAmount;
    // 优惠减免金额
    private Double discountAmount;
    private Integer status; // 0-待支付, 1-已支付, 2-已发货, 3-已签收, 4-已完成, 5-已取消
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    // 收货地址（商品订单使用）
    private String deliveryAddress;
    // 客户端幂等请求ID，避免重复下单
    @Column(unique = true, length = 64)
    private String clientRequestId;

    // 外键：指向 UserGeneral（买家）
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserGeneral user;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems; // 关联的订单项
}
