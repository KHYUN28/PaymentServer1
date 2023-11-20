package com.example.demopayment.event;

import com.example.demopayment.dto.OrderRequest ;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentEvent {

    private final OrderRequest orderRequest;
    private final String message;

}
