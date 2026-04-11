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
import org.dachuang_team.dc_backend_services.services.OrderService;
import org.dachuang_team.dc_backend_services.services.OrderServiceException.OrderStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(Result.success(200, "订单 " + request.getOrderNumber() + " 退款申请提交成功", vo));
    }

    @PostMapping("/refund/process")
    public ResponseEntity<Result<RefundRequestVO>> processRefund(
            @RequestBody @Valid RefundProcessDTO request) throws OrderStateException {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.processRefund(request.getOrderNumber(), currentMerchantId, request.getApprove(), request.getRejectReason());
        String message = request.getApprove() ? "订单 " + request.getOrderNumber() + " 退款申请已批准" : "订单 " + request.getOrderNumber() + " 退款申请已拒绝，理由：" + request.getRejectReason();
        return ResponseEntity.ok(Result.success(200, message, vo));
    }

    @PostMapping("/refund/cancel")
    public ResponseEntity<Result<RefundRequestVO>> cancelRefund(
            @RequestParam String orderNumber) throws OrderStateException {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RefundRequestVO vo = orderService.cancelRefund(orderNumber, currentUserId);
        return ResponseEntity.ok(Result.success(200, "订单 " + orderNumber + " 退款申请已取消", vo));
    }
}
