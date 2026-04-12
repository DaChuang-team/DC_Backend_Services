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
import org.dachuang_team.dc_backend_services.enumeration.RefundStatus;
import org.dachuang_team.dc_backend_services.services.OrderService;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Result<OrderVO>> createOrder(
            @RequestBody @Valid CreateOrderRequestDTO request) throws JsonProcessingException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Order order = orderService.createOrder(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(201, "订单创建成功", OrderVO.from(order)));
    }

    @PostMapping("/pay")
    public ResponseEntity<Result<OrderVO>> payOrder(
            @RequestParam String orderNumber) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.payOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单支付成功", OrderVO.from(order)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Result<String>> confirmOrder(
            @RequestParam String orderNumber) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String orderNo = orderService.confirmOrder(orderNumber, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNo + " 确认成功", null));
    }

    @PostMapping("/refund/request")
    public ResponseEntity<Result<RefundRequestVO>> requestRefund(
            @RequestBody @Valid RefundRequestDTO request) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.requestRefund(request, currentUserId);
        String msg = vo.getStatus().equals(RefundStatus.AUTO_APPROVED) ? "订单 " + request.getOrderNumber() + " 尚未发货，退款申请已自动审核通过" :
                "订单 " + request.getOrderNumber() + " 的退款申请已提交，等待商家审核";
        return ResponseEntity.ok(Result.success(200, msg, vo));
    }

    @PostMapping("/refund/process")
    public ResponseEntity<Result<RefundRequestVO>> processRefund(
            @RequestBody @Valid RefundProcessDTO request) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.processRefund(request.getRefundNo(), currentMerchantId, request.getApprove(), request.getRejectReason());
        String message = request.getApprove() ? "订单 " + vo.getOrderNumber() + " 下的退款请求 " + vo.getRefundNo() + " 已同意" :
                "订单 " + vo.getOrderNumber() + " 下的退款请求 " + vo.getRefundNo() + " 已拒绝，拒绝原因：" + request.getRejectReason();
        return ResponseEntity.ok(Result.success(200, message, vo));
    }

    @PostMapping("/refund/cancel")
    public ResponseEntity<Result<RefundRequestVO>> cancelRefund(
            @RequestParam String refundNo) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.cancelRefund(refundNo, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单编号： " + vo.getOrderNumber() + " 下的退款请求 " + refundNo + " 已成功取消", vo));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Result<OrderVO>> cancelOrder(
            @RequestParam String orderNumber) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.cancelOrder(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 已成功取消", OrderVO.from(order)));
    }

    @PostMapping("/ship")
    public ResponseEntity<Result<OrderVO>> shipOrder(
            @RequestParam String orderNumber,
            @RequestParam String trackingNo) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.shipOrder(orderNumber, currentMerchantId, trackingNo);
        String msg = "订单 " + orderNumber + " 已成功发货，发货方式：" + (Objects.equals(order.getShippingMethod(), "DELIVERY") ? "配送" + "，物流单号：" + trackingNo : "无须发货");
        return ResponseEntity.ok(Result.success(200, msg, OrderVO.from(order)));
    }
}
