package org.dachuang_team.dc_backend_services.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateOrderRequestDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.RefundProcessDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.RefundRequestDTO;
import org.dachuang_team.dc_backend_services.domain.PO.OrderPO.Order;
import org.dachuang_team.dc_backend_services.domain.VO.OrderVO;
import org.dachuang_team.dc_backend_services.domain.VO.RefundRequestVO;
import org.dachuang_team.dc_backend_services.enumeration.OrderStatus;
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
import org.dachuang_team.dc_backend_services.services.OrderService;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderStateException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 买家创建订单
    @PostMapping("/create")
    public ResponseEntity<Result<OrderVO>> createOrder(
            @RequestBody @Valid CreateOrderRequestDTO request) throws JsonProcessingException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Order order = orderService.createOrder(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(201, "订单创建成功", OrderVO.from(order)));
    }

    // 买家支付
    @PostMapping("/pay")
    public ResponseEntity<Result<OrderVO>> payOrder(
            @RequestParam String orderNumber) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.payOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单支付成功", OrderVO.from(order)));
    }

    // 商家确认
    @PostMapping("/confirm")
    public ResponseEntity<Result<OrderVO>> confirmOrder(
            @RequestParam String orderNumber) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.confirmOrder(orderNumber, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "订单 " + order.getOrderNumber() + " 确认成功", OrderVO.from(order)));
    }

    // 买家申请退款
    @PostMapping("/refund/request")
    public ResponseEntity<Result<RefundRequestVO>> requestRefund(
            @RequestBody @Valid RefundRequestDTO request) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.requestRefund(request, currentUserId);
        String msg = vo.getStatus().equals(RefundStatus.AUTO_APPROVED) ? "订单 " + request.getOrderNumber() + " 尚未发货，退款申请已自动审核通过" :
                "订单 " + request.getOrderNumber() + " 的退款申请已提交，等待商家审核";
        return ResponseEntity.ok(Result.success(200, msg, vo));
    }

    // 商家处理退款
    @PostMapping("/refund/process")
    public ResponseEntity<Result<RefundRequestVO>> processRefund(
            @RequestBody @Valid RefundProcessDTO request) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.processRefund(request.getRefundNo(), currentMerchantId, request.getApprove(), request.getRejectReason());
        String message = request.getApprove() ? "订单 " + vo.getOrderNumber() + " 下的退款请求 " + vo.getRefundNo() + " 已同意" :
                "订单 " + vo.getOrderNumber() + " 下的退款请求 " + vo.getRefundNo() + " 已拒绝，拒绝原因：" + request.getRejectReason();
        return ResponseEntity.ok(Result.success(200, message, vo));
    }

    // 买家撤销退款
    @PostMapping("/refund/cancel")
    public ResponseEntity<Result<RefundRequestVO>> cancelRefund(
            @RequestParam String refundNo) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.cancelRefund(refundNo, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已成功取消", vo));
    }

    // 买家取消订单
    @PostMapping("/cancel")
    public ResponseEntity<Result<OrderVO>> cancelOrder(
            @RequestParam String orderNumber) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.cancelOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 已成功取消", OrderVO.from(order)));
    }

    // 商家发货
    @PostMapping("/ship")
    public ResponseEntity<Result<OrderVO>> shipOrder(
            @RequestParam String orderNumber,
            @RequestParam (required = false) String trackingNo) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.shipOrder(orderNumber, currentMerchantId, trackingNo);
        String msg = "订单 " + orderNumber + " 已成功发货，发货方式：" + (Objects.equals(order.getShippingMethod(), "DELIVERY") ? "配送" + "，物流单号：" + trackingNo : "无须发货");
        return ResponseEntity.ok(Result.success(200, msg, OrderVO.from(order)));
    }

    //买家签收
    @PostMapping("/receive")
    public ResponseEntity<Result<OrderVO>> receiveOrder(
            @RequestParam String orderNumber) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.receiveOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 已签收", OrderVO.from(order)));
    }

    // 买家确认收货
    @PostMapping("/complete")
    public ResponseEntity<Result<OrderVO>> completeOrder(
            @RequestParam String orderNumber) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.completeOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 已确认收货", OrderVO.from(order)));
    }

    // 买家退件发货
    @PostMapping("/refund/ship-return")
    public ResponseEntity<Result<RefundRequestVO>> refundReturnShipped(
            @RequestParam String refundNo,
            @RequestParam String returnTrackingNo) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.submitReturnTracking(refundNo, currentUserId, returnTrackingNo);
        return ResponseEntity.ok(Result.success(200, "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已上传退件物流，单号：" + returnTrackingNo, vo));
    }

    // 商家签收退件
    @PostMapping("/refund/return-receive")
    public ResponseEntity<Result<RefundRequestVO>> refundReturnReceived(
            @RequestParam String refundNo) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.receiveReturnedProduct(refundNo, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已确认收到退件", vo));
    }

    // 商家处理退件
    @PostMapping("/refund/handel-return")
    public ResponseEntity<Result<RefundRequestVO>> refundHandleReturn(
            @RequestParam String refundNo,
            @RequestParam Boolean approve,
            @RequestParam(required = false) String rejectReason) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.handleRefundAfterReturnReceived(refundNo, currentMerchantId, approve, rejectReason);
        String message = vo.getStatus().equals(RefundStatus.APPROVED) ? "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已同意退件，正在等待系统自动退款" :
                "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已拒绝，拒绝原因：" + rejectReason;
        return ResponseEntity.ok(Result.success(200, message, vo));
    }

    // 买家查询订单列表
    @GetMapping("/user/orders")
    public ResponseEntity<Result<Map<String, Object>>> getUserOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!OrderStatus.isValidStatus(String.valueOf(status)))
            status = null;
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderService.getBuyerOrders(currentUserId, status, pageable);

        return getOrdersMapResponseEntity(orderPage);
    }

    // 商家查询订单列表
    @GetMapping("/seller/orders")
    public ResponseEntity<Result<Map<String, Object>>> getSellerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!OrderStatus.isValidStatus(String.valueOf(status)))
            status = null;
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderService.getSellerOrders(currentMerchantId, status, pageable);

        return getOrdersMapResponseEntity(orderPage);
    }

    // 买家查询退款列表
    @GetMapping("/user/refund/list")
    public ResponseEntity<Result<Map<String, Object>>> getUserRefundList(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!RefundStatus.isValidStatus(String.valueOf(status)))
            status = null;
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("requestTime").descending());
        Page<RefundRequestVO> refundPage = orderService.getBuyerRefundRequests(currentUserId, status, pageable);

        return getRefundsMapResponseEntity(refundPage);
    }

    // 商家查询退款列表
    @GetMapping("/seller/refund/list")
    public ResponseEntity<Result<Map<String, Object>>> getSellerRefundList(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!RefundStatus.isValidStatus(String.valueOf(status)))
            status = null;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestTime").descending());
        Page<RefundRequestVO> refundPage = orderService.getSellerRefundRequests(currentMerchantId, status, pageable);

        return getRefundsMapResponseEntity(refundPage);
    }

    // 买家查询某个订单下的退款记录
    @GetMapping("/user/order/refund/list")
    public ResponseEntity<Result<List<RefundRequestVO>>> getOrderRefundListForBuyer(
            @RequestParam String orderNumber) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RefundRequestVO> refundList = orderService.getRefundRequestsByOrderNumber(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 下的退款记录查询成功", refundList));
    }

    // 商家查询某个订单下的退款记录
    @GetMapping("/seller/order/refund/list")
    public ResponseEntity<Result<List<RefundRequestVO>>> getOrderRefundListForSeller(
            @RequestParam String orderNumber) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RefundRequestVO> refundList = orderService.getRefundRequestsByOrderNumber(orderNumber, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 下的退款记录查询成功", refundList));
    }

    // 买家根据退款号模糊查询退款记录
    @GetMapping("/user/refund/search")
    public ResponseEntity<Result<Map<String, Object>>> getUserRefundSearch(
            @RequestParam String refundNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestTime").descending());
        Page<RefundRequestVO> refundPage = orderService.getRefundRequestsByRefundNo(refundNo, currentUserId, pageable);
        return getRefundsMapResponseEntity(refundPage);
    }

    // 商家根据退款号模糊查询退款记录
    @GetMapping("/seller/refund/search")
    public ResponseEntity<Result<Map<String, Object>>> getSellerRefundSearch(
            @RequestParam String refundNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestTime").descending());
        Page<RefundRequestVO> refundPage = orderService.getRefundRequestsByRefundNo(refundNo, currentMerchantId, pageable);
        return getRefundsMapResponseEntity(refundPage);
    }

    // 买家根据订单号模糊查询订单记录
    @GetMapping("/user/order/search")
    public ResponseEntity<Result<Map<String, Object>>> searchUserOrders(
            @RequestParam String orderNumber,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderService.getOrdersByOrderNumber(orderNumber, currentUserId, pageable);
        return getOrdersMapResponseEntity(orderPage);
    }

    // 商家根据订单号模糊查询订单记录
    @GetMapping("/seller/order/search")
    public ResponseEntity<Result<Map<String, Object>>> searchSellerOrders(
            @RequestParam String orderNumber,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderService.getOrdersByOrderNumber(orderNumber, currentMerchantId, pageable);
        return getOrdersMapResponseEntity(orderPage);
    }

    @NotNull
    private ResponseEntity<Result<Map<String, Object>>> getOrdersMapResponseEntity(Page<Order> orderPage) {
        List<OrderVO> orderVOList = orderPage.getContent().stream()
                .map(OrderVO::from)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderVOList);
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("currentPage", orderPage.getNumber() + 1);

        return ResponseEntity.ok(Result.success(200, "订单列表查询成功", response));
    }

    @NotNull
    private ResponseEntity<Result<Map<String, Object>>> getRefundsMapResponseEntity(Page<RefundRequestVO> refundPage) {
        Map<String, Object> response = new HashMap<>();
        response.put("refunds", refundPage.getContent());
        response.put("totalItems", refundPage.getTotalElements());
        response.put("totalPages", refundPage.getTotalPages());
        response.put("currentPage", refundPage.getNumber() + 1);

        return ResponseEntity.ok(Result.success(200, "退款列表查询成功", response));
    }

}
