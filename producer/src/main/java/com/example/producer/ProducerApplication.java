package com.example.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ProducerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProducerApplication.class, args);

        PaymentProducer paymentProducer = context.getBean(PaymentProducer.class);

        // 정보 설정
        String order_id = "qwerty5828";
        int amount = 1000;
//        String card_number = "5465-9699-1234-5678";
//        String expiry = "2027-05";
//        String birth = "940123";
//        String pwd_2digit = "12";
//        String cvc = "123";
//        int cancel_amount = 100;

        // Kafka 토픽으로 payment 메시지 보내기
        paymentProducer.sendPaymentMessage(order_id, amount);
//        paymentProducer.sendPaymentMessage(order_id, amount, card_number, expiry, birth, pwd_2digit, cvc);
        // 애플리케이션 종료
        context.close();
    }
}
