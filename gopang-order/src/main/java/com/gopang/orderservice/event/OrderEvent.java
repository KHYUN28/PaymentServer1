package com.gopang.orderservice.event;

import com.gopang.orderservice.dto.PaymentRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderEvent {

    private final PaymentRequest paymentRequest;

    private final String message;

}
