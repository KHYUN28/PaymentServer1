package com.kkh.gopangpayment.domain;

import com.kkh.gopangpayment.dto.Paymentdto.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Card {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ORDER_ID")
    public Long order_id;
    @Column(nullable = false)
    private String merchant_uid;
    @Column(nullable = false)
    private int amount;
    @Column(nullable = false)
    private String card_number;
    @Column(nullable = false)
    private String expiry;
    @Column(nullable = false)
    private String birth;
    @Column(nullable = false)
    private String pwd_2digit;
    @Column(nullable = false)
    private String cvc;
    @CreatedDate
    private LocalDateTime createdAt;

    public String getMerchant_uid() {
        return merchant_uid;
    }

    public Card(String merchant_uid, int amount, String card_number, String expiry, String birth, String pwd_2digit, String cvc,
                LocalDateTime createdAt) {
        this.merchant_uid = merchant_uid;
        this.amount = amount;
        this.card_number = card_number;
        this.expiry = expiry;
        this.birth = birth;
        this.pwd_2digit = pwd_2digit;
        this.cvc = cvc;
        this.createdAt = createdAt;
    }

}
