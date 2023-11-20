package com.example.producer;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public PaymentProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentMessage(String order_id, int amount) {
//    public void sendPaymentMessage(String order_id, int amount, String card_number, String expiry, String birth, String pwd_2digit, String cvc) {
        // JSON 데이터 to send to Kafka
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("order_id", order_id);
        jsonBody.put("amount", amount);
//        jsonBody.put("card_number", card_number);
//        jsonBody.put("expiry", expiry);
//        jsonBody.put("birth", birth);
//        jsonBody.put("pwd_2digit", pwd_2digit);
//        jsonBody.put("cvc", cvc);

        kafkaTemplate.send("paymentRequest-topic", String.valueOf(jsonBody));
    }
}
