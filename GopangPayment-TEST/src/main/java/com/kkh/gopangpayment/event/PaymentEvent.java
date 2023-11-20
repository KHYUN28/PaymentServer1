package com.kkh.gopangpayment.event;

import com.kkh.gopangpayment.dto.PaymentRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentEvent {

    private PaymentRequest paymentRequest;

    private String message;

    public PaymentEvent(PaymentRequest payment, String message) {
    }


}
