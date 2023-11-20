package com.gopang.orderservice.dto;

import lombok.ToString;

@ToString
public class CancelStatus {
    String merchant_Uid;
    int cancelAmount;
    int amount;
    int remainingBalance;
    String status;
}