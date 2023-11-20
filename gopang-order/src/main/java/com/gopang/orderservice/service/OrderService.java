package com.gopang.orderservice.service;

import com.gopang.orderservice.domain.OrderHistory;
import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.dto.Order;
import com.gopang.orderservice.dto.PaymentRequest;
import com.gopang.orderservice.event.OrderCancelEvent;
import com.gopang.orderservice.event.OrderEvent;
import com.gopang.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    // 주문 저장을 위한 Repository 주입
    private final OrderRepository orderRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher 주입
    private final ApplicationEventPublisher eventPublisher;

    // 주문 요청 처리 메서드
    @Transactional
    public Orders register(Order.OrderRequest request) throws Exception {
        Orders orders= null;
        OrderHistory history = null;

        try {
            // 주문 정보 생성 및 저장
            orders = Orders.builder()
                    .user_id(request.user_id)
                    .cartitem_id(request.cartitem_id)
                    .reciever_name(request.reciever_name)
                    .reciever_phone(request.reciever_phone)
                    .reciever_addr(request.reciever_addr)
                    .amount(request.amount)
                    .deleteYn("N")
                    .build();

            orders = orderRepository.save(orders);

            // 결제 요청을 위한 PaymentRequest 생성
            PaymentRequest payment = PaymentRequest.builder()
                    .order_id(orders.getId().toString())
                    .amount(orders.amount.intValue())
                    .build();

            // 로깅을 위한 메시지 생성 및 이벤트 발행
            String message = payment.getOrder_id() + "번 주문 결제 요청됨.";
            eventPublisher.publishEvent(new OrderEvent(payment, message));

        } catch (Exception e) {
            // 예외 발생 시 잘못된 요청 예외 전파
            throw new Exception("잘못된 요청입니다.");
        }
        return orders;
    }

    // 주문 취소 처리 메서드
    @Transactional
    public void orderCancel(Long orderId) {
        // 주문 정보 조회
        Orders orders = orderRepository.findById(orderId).get();

        // 결제 취소를 위한 PaymentRequest 생성
        PaymentRequest payment = PaymentRequest.builder()
                .order_id(orders.getId().toString())
                .amount(orders.amount.intValue())
                .build();

        // 로깅을 위한 메시지 생성 및 주문 취소 이벤트 발행
        String message = payment.getOrder_id() + "번 주문 결제 취소 요청됨.";
        eventPublisher.publishEvent(new OrderCancelEvent(payment, message));

        // 주문 정보 삭제
        orderRepository.deleteById(orderId);
    }
}
