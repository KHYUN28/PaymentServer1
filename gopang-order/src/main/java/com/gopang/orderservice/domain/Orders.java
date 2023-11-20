package com.gopang.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE orders SET delete_yn='Y' WHERE order_id=? ")
public class Orders {

    @Id @GeneratedValue
    @Column(name="order_id")
    private Long id;

    public Long user_id;

    public Long cartitem_id;

    public Long amount;

    public String reciever_name;

    public String reciever_phone;

    public String reciever_addr;

    public String deleteYn;

    public LocalDateTime time;

}
