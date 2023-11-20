package com.kkh.gopangpayment.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentRequest {
    String order_id;
    String status;
}
