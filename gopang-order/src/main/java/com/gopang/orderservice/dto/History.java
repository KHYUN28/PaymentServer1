package com.gopang.orderservice.dto;

import com.gopang.orderservice.domain.OrderState;
import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

public class History {

    @Getter
    @Builder
    public static class HistoryRequest {
        public PaymentStatus paymentStatus;

        public Long orderId;
    }

    @Builder
    public static class HistoryResponse {
        public Long id;

        // 주문자 고유번호
        public Long user_id;

        // 주문 상품 고유번호
        public Long item_id;

        // 총 주문금액
        public Long order_amount;

        public Orders orders;

        // 주문상태
        public OrderState order_state;
    }

}
