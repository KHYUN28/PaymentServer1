package com.example.demopayment.dto.Canceldto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class CancelStatus {
    public String orderId;
    public int cancelAmount;
    public int amount;
    public int remainingBalance;
    public String status;

    public CancelStatus(String orderId, int cancelAmount, int amount, int remainingBalance, String status) {
        this.orderId = orderId;
        this.cancelAmount = cancelAmount;
        this.amount = amount;
        this.remainingBalance = remainingBalance;
        this.status = status;
    }
}
