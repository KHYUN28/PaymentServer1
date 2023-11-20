package com.gopang.orderservice.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentRequest {
    String order_id;
    int amount;
}
