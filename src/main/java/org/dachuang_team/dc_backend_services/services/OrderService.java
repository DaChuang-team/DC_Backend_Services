package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.pojo.HotelHomestay;
import org.dachuang_team.dc_backend_services.pojo.Order;
import org.dachuang_team.dc_backend_services.enumeration.OrderItemType;
import org.dachuang_team.dc_backend_services.pojo.ProductPO.ProductOrderItem;
import org.dachuang_team.dc_backend_services.pojo.HotelOrderItem;
import org.dachuang_team.dc_backend_services.pojo.ProductPO.Product;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.pojo.Dto.OrderDTO;
import org.dachuang_team.dc_backend_services.repository.HotelHomestayRepository;
import org.dachuang_team.dc_backend_services.repository.ProductOrderItemRepository;
import org.dachuang_team.dc_backend_services.repository.HotelOrderItemRepository;
import org.dachuang_team.dc_backend_services.repository.OrderRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {
    // 订单状态常量
    public static final int STATUS_UNPAID = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_SHIPPED = 2;
    public static final int STATUS_RECEIVED = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELED = 5;
    public static final int STATUS_BOOKED = 6;
    public static final int STATUS_CHECKED_IN = 7;

    private static final String ITEM_TYPE_PRODUCT = "PRODUCT";
    private static final String ITEM_TYPE_HOTEL = "HOTEL";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductOrderItemRepository productOrderItemRepository;

    @Autowired
    private HotelOrderItemRepository hotelOrderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HotelHomestayRepository hotelHomestayRepository;

    @Autowired
    private UserRepository userRepository;

    // 订单预览，计算价格并返回快照信息
    public OrderDTO.PreviewResponse previewOrder(Long userId, OrderDTO.PreviewRequest request) {
        validateUser(userId);
        CalculationContext context = buildCalculationContext(
                request.getItemType(),
                request.getItemId(),
                request.getQuantity(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getRoomCount()
        );

        OrderDTO.PreviewResponse response = new OrderDTO.PreviewResponse();
        response.setItemType(context.itemType.name());
        response.setItemId(context.itemId);
        response.setTotalAmount(context.totalAmount);
        response.setPriceVersion(buildPriceVersion(context));
        response.setItems(context.items);
        return response;
    }

    @Transactional
    // 创建订单并写入对应订单项
    public OrderDTO.OrderSummaryResponse createOrder(Long userId, OrderDTO.CreateRequest request) {
        UserGeneral user = validateUser(userId);
        if (request.getClientRequestId() == null || request.getClientRequestId().isBlank()) {
            throw new IllegalArgumentException("clientRequestId 不能为空");
        }
        Order existing = orderRepository.findByClientRequestId(request.getClientRequestId()).orElse(null);
        if (existing != null) {
            ensureOrderOwnership(existing, userId);
            return buildOrderSummary(existing);
        }

        CalculationContext context = buildCalculationContext(
                request.getItemType(),
                request.getItemId(),
                request.getQuantity(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getRoomCount()
        );

        String serverPriceVersion = buildPriceVersion(context);
        if (!Objects.equals(serverPriceVersion, request.getPriceVersion())) {
            throw new IllegalArgumentException("价格信息已变化，请重新预览后下单");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(STATUS_UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(user);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDiscountAmount(0D);
        order.setTotalPrice(context.totalAmount);
        order.setPayAmount(context.totalAmount);
        order.setClientRequestId(request.getClientRequestId());
        Order savedOrder = orderRepository.save(order);

        buildOrderItems(savedOrder, context);

        return buildOrderSummary(savedOrder);
    }

    @Transactional
    // 支付订单
    public OrderDTO.PayResponse payOrder(Long userId, Long orderId, OrderDTO.PayRequest request) {
        if (request.getPayChannel() == null || request.getPayChannel().isBlank()) {
            throw new IllegalArgumentException("payChannel 不能为空");
        }
        Order order = getOwnedOrder(userId, orderId);
        if (!Objects.equals(order.getStatus(), STATUS_UNPAID)) {
            throw new IllegalArgumentException("当前状态不可支付");
        }
        order.setStatus(STATUS_PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderDTO.PayResponse response = new OrderDTO.PayResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus());
        response.setPaidAt(order.getPaidAt());
        return response;
    }

    // 查询订单详情
    public OrderDTO.OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        return buildOrderDetail(order);
    }

    // 分页查询我的订单
    public List<OrderDTO.OrderSummaryResponse> getMyOrders(Long userId, Integer page, Integer size, Integer status) {
        validateUser(userId);
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> result;
        if (status == null) {
            result = orderRepository.findByUserUserId(userId, pageable);
        } else {
            result = orderRepository.findByUserUserIdAndStatus(userId, status, pageable);
        }
        List<OrderDTO.OrderSummaryResponse> list = new ArrayList<>();
        for (Order order : result.getContent()) {
            list.add(buildOrderSummary(order));
        }
        return list;
    }

    @Transactional
    // 取消订单
    public OrderDTO.CancelResponse cancelOrder(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (!Objects.equals(order.getStatus(), STATUS_UNPAID)) {
            throw new IllegalArgumentException("只有待支付订单允许取消");
        }
        order.setStatus(STATUS_CANCELED);
        orderRepository.save(order);

        OrderDTO.CancelResponse response = new OrderDTO.CancelResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus());
        return response;
    }

    // 校验并返回用户
    private UserGeneral validateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    // 校验并获取订单
    private Order getOwnedOrder(Long userId, Long orderId) {
        validateUser(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        ensureOrderOwnership(order, userId);
        return order;
    }

    // 权限校验，确保订单归属当前用户
    private void ensureOrderOwnership(Order order, Long userId) {
        if (order.getUser() == null || !Objects.equals(order.getUser().getUserId(), userId)) {
            throw new IllegalArgumentException("无权访问该订单");
        }
    }

    // 生成计算上下文
    private CalculationContext buildCalculationContext(
            String itemType,
            Long itemId,
            Integer quantity,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer roomCount
    ) {
        if (itemType == null || itemType.isBlank()) {
            throw new IllegalArgumentException("itemType 不能为空");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("itemId 不能为空");
        }

        String normalizedType = itemType.toUpperCase(Locale.ROOT);
        if (ITEM_TYPE_PRODUCT.equals(normalizedType)) {
            return buildProductCalculation(itemId, quantity);
        }
        if (ITEM_TYPE_HOTEL.equals(normalizedType)) {
            return buildHotelCalculation(itemId, checkInDate, checkOutDate, roomCount);
        }
        throw new IllegalArgumentException("itemType 仅支持 PRODUCT 或 HOTEL");
    }

    // 计算商品订单价格与快照
    private CalculationContext buildProductCalculation(Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }
        Product product = productRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (Boolean.FALSE.equals(product.getApproved())) {
            throw new IllegalArgumentException("商品未通过审核，暂不可下单");
        }

        double unitPrice = product.getPrice();
        double total = unitPrice * quantity;

        OrderDTO.OrderItemView itemView = new OrderDTO.OrderItemView();
        itemView.setItemType(OrderItemType.PRODUCT.name());
        itemView.setItemId(product.getProductId());
        itemView.setItemName(product.getProductName());
        itemView.setQuantity(quantity);
        itemView.setUnitPrice(unitPrice);
        itemView.setLineAmount(total);

        CalculationContext context = new CalculationContext();
        context.itemType = OrderItemType.PRODUCT;
        context.itemId = product.getProductId();
        context.itemName = product.getProductName();
        context.unitPrice = unitPrice;
        context.quantity = quantity;
        context.roomCount = null;
        context.nightCount = null;
        context.checkInDate = null;
        context.checkOutDate = null;
        context.totalAmount = total;
        context.items = List.of(itemView);
        context.product = product;
        return context;
    }

    // 计算酒店订单价格与快照
    private CalculationContext buildHotelCalculation(
            Long itemId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer roomCount
    ) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("酒店订单必须传入 checkInDate 和 checkOutDate");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate 必须晚于 checkInDate");
        }
        if (roomCount == null || roomCount <= 0) {
            throw new IllegalArgumentException("roomCount 必须大于 0");
        }
        HotelHomestay hotel = hotelHomestayRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));
        if (Boolean.FALSE.equals(hotel.getIsAvailable())) {
            throw new IllegalArgumentException("酒店当前不可预订");
        }
        if (hotel.getPrice() == null || hotel.getPrice() <= 0) {
            throw new IllegalArgumentException("酒店价格异常，暂不可下单");
        }

        int nightCount = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double unitPrice = hotel.getPrice();
        double total = unitPrice * roomCount * nightCount;

        OrderDTO.OrderItemView itemView = new OrderDTO.OrderItemView();
        itemView.setItemType(OrderItemType.HOTEL.name());
        itemView.setItemId(hotel.getHotelId());
        itemView.setItemName(hotel.getHotelName());
        itemView.setRoomCount(roomCount);
        itemView.setNightCount(nightCount);
        itemView.setCheckInDate(checkInDate);
        itemView.setCheckOutDate(checkOutDate);
        itemView.setUnitPrice(unitPrice);
        itemView.setLineAmount(total);

        CalculationContext context = new CalculationContext();
        context.itemType = OrderItemType.HOTEL;
        context.itemId = hotel.getHotelId();
        context.itemName = hotel.getHotelName();
        context.unitPrice = unitPrice;
        context.quantity = 1;
        context.roomCount = roomCount;
        context.nightCount = nightCount;
        context.checkInDate = checkInDate;
        context.checkOutDate = checkOutDate;
        context.totalAmount = total;
        context.items = List.of(itemView);
        context.hotel = hotel;
        return context;
    }

    // 写入不同类型的订单项
    private void buildOrderItems(Order order, CalculationContext context) {
        if (OrderItemType.PRODUCT.equals(context.itemType)) {
            ProductOrderItem item = new ProductOrderItem();
            item.setOrder(order);
            item.setItemNameSnapshot(context.itemName);
            item.setUnitPriceSnapshot(context.unitPrice);
            item.setPriceAtOrder(context.totalAmount);
            item.setQuantity(context.quantity == null ? 1 : context.quantity);
            item.setProduct(context.product);
            productOrderItemRepository.save(item);
            return;
        }

        HotelOrderItem item = new HotelOrderItem();
        item.setOrder(order);
        item.setItemNameSnapshot(context.itemName);
        item.setUnitPriceSnapshot(context.unitPrice);
        item.setPriceAtOrder(context.totalAmount);
        item.setRoomCount(context.roomCount);
        item.setNightCount(context.nightCount);
        item.setCheckInDate(context.checkInDate);
        item.setCheckOutDate(context.checkOutDate);
        item.setHotelId(context.hotel.getHotelId());
        hotelOrderItemRepository.save(item);
    }

    // 生成订单号
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // 构建价格版本用于防篡改
    private String buildPriceVersion(CalculationContext context) {
        String raw = String.join("|",
                context.itemType.name(),
                String.valueOf(context.itemId),
                String.valueOf(context.unitPrice),
                String.valueOf(context.quantity),
                String.valueOf(context.roomCount),
                String.valueOf(context.nightCount),
                String.valueOf(context.checkInDate),
                String.valueOf(context.checkOutDate),
                String.valueOf(context.totalAmount));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("构建价格版本失败", e);
        }
    }

    // 生成订单摘要
    private OrderDTO.OrderSummaryResponse buildOrderSummary(Order order) {
        OrderDTO.OrderSummaryResponse response = new OrderDTO.OrderSummaryResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus());
        response.setPayAmount(order.getPayAmount());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    // 生成订单详情并合并订单项
    private OrderDTO.OrderDetailResponse buildOrderDetail(Order order) {
        List<OrderDTO.OrderItemView> itemViews = new ArrayList<>();
        List<ProductOrderItem> productItems = productOrderItemRepository.findByOrderOrderId(order.getOrderId());
        for (ProductOrderItem item : productItems) {
            OrderDTO.OrderItemView itemView = new OrderDTO.OrderItemView();
            itemView.setItemType(OrderItemType.PRODUCT.name());
            itemView.setItemName(item.getItemNameSnapshot());
            itemView.setQuantity(item.getQuantity());
            itemView.setUnitPrice(item.getUnitPriceSnapshot());
            itemView.setLineAmount(item.getPriceAtOrder());
            if (item.getProduct() != null) {
                itemView.setItemId(item.getProduct().getProductId());
            }
            itemViews.add(itemView);
        }
        List<HotelOrderItem> hotelItems = hotelOrderItemRepository.findByOrderOrderId(order.getOrderId());
        for (HotelOrderItem item : hotelItems) {
            OrderDTO.OrderItemView itemView = new OrderDTO.OrderItemView();
            itemView.setItemType(OrderItemType.HOTEL.name());
            itemView.setItemName(item.getItemNameSnapshot());
            itemView.setQuantity(1);
            itemView.setRoomCount(item.getRoomCount());
            itemView.setNightCount(item.getNightCount());
            itemView.setCheckInDate(item.getCheckInDate());
            itemView.setCheckOutDate(item.getCheckOutDate());
            itemView.setUnitPrice(item.getUnitPriceSnapshot());
            itemView.setLineAmount(item.getPriceAtOrder());
            itemView.setItemId(item.getHotelId());
            itemViews.add(itemView);
        }

        OrderDTO.OrderDetailResponse response = new OrderDTO.OrderDetailResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus());
        response.setTotalPrice(order.getTotalPrice());
        response.setPayAmount(order.getPayAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaidAt(order.getPaidAt());
        response.setItems(itemViews);
        return response;
    }

    private static class CalculationContext {
        private OrderItemType itemType;
        private Long itemId;
        private String itemName;
        private Double unitPrice;
        private Integer quantity;
        private Integer roomCount;
        private Integer nightCount;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Double totalAmount;
        private List<OrderDTO.OrderItemView> items;
        private Product product;
        private HotelHomestay hotel;
    }
}
