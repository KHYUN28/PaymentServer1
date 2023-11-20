package com.gopang.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gopang.orderservice.domain.PayloadType;
import lombok.Builder;

public class Order {

    public static class OrderRequest {
        public Long user_id;

        public Long cartitem_id;

        public Long amount;

        public String reciever_name;

        public String reciever_phone;

        public String reciever_addr;
    }

    @Builder
    public static class PaymentRequest {
        public Long order_id;

        public Long amount;

        @JsonProperty("payload_type")
        public PayloadType payloadType;
    }
}
