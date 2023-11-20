package com.gopang.orderservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory {
    @Id @GeneratedValue
    @Column(name = "history_id")
    public Long id;

    // 주문자 고유번호
    public Long user_id;

    // 주문 상품 고유번호
    public Long item_id;
    
    // 총 주문금액
    public Long order_amount;

    @OneToOne
    @JoinColumn(name = "order_id")
    public Orders orders;

    // 주문상태
    @Column
    @Enumerated(EnumType.STRING)
    public OrderState order_state;

}
