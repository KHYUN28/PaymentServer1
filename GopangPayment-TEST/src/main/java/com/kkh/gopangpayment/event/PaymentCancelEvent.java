package com.kkh.gopangpayment.event;


import com.kkh.gopangpayment.dto.PaymentRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentCancelEvent {
    private PaymentRequest paymentRequest;

    private String message;

}
