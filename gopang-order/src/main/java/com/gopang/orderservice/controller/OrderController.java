package com.gopang.orderservice.controller;

import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.dto.Order;
import com.gopang.orderservice.service.HistoryService;
import com.gopang.orderservice.service.OrderService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final HistoryService historyService;

    @PostMapping
    // 주문 요청
    public ResponseEntity<?> order(@RequestBody Order.OrderRequest order) throws Exception {

        Orders regiOrder = orderService.register(order);

        historyService.register(regiOrder, order);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 주문 취소 요청
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> orderCancel(@PathVariable Long orderId) throws Exception {
        try{
            orderService.orderCancel(orderId);
        } catch (Exception e) {
            throw new Exception("요청중 오류 발생");
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
