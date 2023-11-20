package com.gopang.orderservice.dto;

import com.gopang.orderservice.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatusUpdate {
    public PaymentStatus paymentStatus;

    public Long orderId;
}
