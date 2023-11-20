package com.example.demopayment.event;

import com.example.demopayment.dto.Canceldto.CancelStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentCancelEvent {

    private final CancelStatus cancelStatus;
    private final String message;

}
