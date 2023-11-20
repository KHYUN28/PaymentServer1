package com.gopang.orderservice.service;

import com.gopang.orderservice.domain.OrderHistory;
import com.gopang.orderservice.domain.OrderState;
import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.domain.PaymentStatus;
import com.gopang.orderservice.dto.CancelStatus;
import com.gopang.orderservice.dto.History;
import com.gopang.orderservice.dto.Order;
import com.gopang.orderservice.dto.StatusUpdate;
import com.gopang.orderservice.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class HistoryService {

    // 주문 내역 저장을 위한 Repository 주입
    private final HistoryRepository historyRepository;

    // StatusUpdate 이벤트를 처리하는 Consumer Bean
    @Bean
    public Consumer<StatusUpdate> consumerBinding() {
        // StatusUpdate 이벤트 수신 시 stateUpdate 메서드 호출
        return pay -> stateUpdate(pay.getOrderId(), pay.getPaymentStatus());
    }

    // CancelStatus 이벤트를 처리하는 Consumer Bean
    @Bean
    public Consumer<CancelStatus> cancelBinding() {
        // CancelStatus 이벤트 수신 시 간단한 출력 수행
        return cancel -> System.out.println("cancel 토픽 : " + cancel);
    }

    // 주문 내역 등록 메서드
    public void register(Orders orders, Order.OrderRequest orderRequest) {
        // OrderHistory 객체 생성 및 저장
        OrderHistory history = OrderHistory.builder()
                .orders(orders)
                .user_id(orderRequest.user_id)
                .item_id(orderRequest.cartitem_id)
                .order_amount(orderRequest.amount)
                .order_state(OrderState.DELIVERYREADY)
                .build();

        historyRepository.save(history);
    }

    // 주문 상태 업데이트 메서드
    public void stateUpdate(Long orderId, PaymentStatus status) {
        // 주문 내역 조회
        Optional<OrderHistory> history = historyRepository.findById(orderId);

        OrderHistory orderHistory;

        // 주문 내역이 존재하는 경우 상태 업데이트 수행
        if (history.isPresent()) {
            orderHistory = history.get();
            if (status == PaymentStatus.PAYCOMPLETE) {
                orderHistory.setOrder_state(OrderState.PAYCOMPLETE);
            } else {
                orderHistory.setOrder_state(OrderState.CANCEL);
            }

            // 업데이트된 주문 내역 저장
            historyRepository.save(orderHistory);
        }
    }

    // 전체 주문 내역 조회 메서드
    // 주문 내역의 아이템 각 항목당 링크 추가 필요
    @Transactional(readOnly = true)
    public List<History.HistoryResponse> getAllHistory(Long userId) {
        // 사용자별 주문 내역 조회
        List<OrderHistory> allByUserId = historyRepository.findAllByUserId(userId);

        // OrderHistory를 HistoryResponse로 변환하여 반환
        return allByUserId.stream().map(orderHistory ->
                History.HistoryResponse.builder()
                        .id(orderHistory.getId())
                        .user_id(orderHistory.getUser_id())
                        .item_id(orderHistory.getItem_id())
                        .order_state(orderHistory.getOrder_state())
                        .order_amount(orderHistory.getOrder_amount())
                        .orders(orderHistory.getOrders())
                        .build()
        ).toList();
    }
}
