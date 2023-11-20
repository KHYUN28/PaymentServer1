package com.example.demopayment.dto;

import com.example.demopayment.dto.Paymentdto.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderRequest {
    String order_id;
    PaymentStatus StatusUpdate;
}
