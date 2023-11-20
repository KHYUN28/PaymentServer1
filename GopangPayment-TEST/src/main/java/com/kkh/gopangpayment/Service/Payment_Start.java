package com.kkh.gopangpayment.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkh.gopangpayment.authentication.TokenService;
import com.kkh.gopangpayment.domain.Card;
import com.kkh.gopangpayment.dto.PaymentRequest;
import com.kkh.gopangpayment.dto.Paymentdto.PaymentStatus;
import com.kkh.gopangpayment.event.PaymentEvent;
import com.kkh.gopangpayment.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class Payment_Start {

    @Value("${portone.apiBaseUrl}")
    private String API_BASE_URL;
    @Value("${portone.paymentstart}")
    private String PAYMENT_ENDPOINT;

    @Autowired
    private CardRepository cardRepository;

    // 이벤트 발행을 위한 ApplicationEventPublisher 주입
    private final ApplicationEventPublisher eventPublisher;

    public Payment_Start(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//    private final ObjectMapper objectMapper = new ObjectMapper();

//    @KafkaListener(topics = "paymentRequest-topic", groupId = "payment")
    @Transactional
    public String Payment(String jsonString) throws IOException, JSONException{

        TokenService tokenService = new TokenService();
        String accessToken = tokenService.getToken();

        RestTemplate restTemplate = new RestTemplate();
        JSONObject kafkajsonBody = new JSONObject(jsonString);

        System.err.println("KafkaPaymentReceived : " + jsonString);

        String KafkaMerchant_uid = kafkajsonBody.optString("order_id", null);
        int KafkaAmount = kafkajsonBody.optInt("amount", 0);

        String KafkaCard_number = "5465-9699-1234-5678";
        String KafkaExpiry = "2027-05";
        String KafkaBirth = "940123";
        String KafkaPwd_2digit = "12";
        String KafkaCvc = "123";

//        String KafkaCard_number = kafkajsonBody.optString("card_number", null);
//        String KafkaExpiry = kafkajsonBody.optString("expiry", null);
//        String KafkaBirth = kafkajsonBody.optString("birth", null);
//        String KafkaPwd_2digit = kafkajsonBody.optString("pwd_2digit", null);
//        String KafkaCvc = kafkajsonBody.optString("cvc", null);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("merchant_uid", KafkaMerchant_uid);
        jsonBody.put("amount", KafkaAmount);
        jsonBody.put("card_number", KafkaCard_number);
        jsonBody.put("expiry", KafkaExpiry);
        jsonBody.put("birth", KafkaBirth);
        jsonBody.put("pwd_2digit", KafkaPwd_2digit);
        jsonBody.put("cvc", KafkaCvc);

        // JSON 데이터와 적절한 미디어 타입을 설정하여 HttpEntity를 만듭니다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody.toString(), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_BASE_URL + PAYMENT_ENDPOINT,
                requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(responseEntity.getBody());

        String padPaymentStatus = responseJson.path("response").path("status").asText();

        if (Objects.equals(padPaymentStatus, "paid")) {

            // 파싱할 데이터 추출
            String pasMerchant_uid = responseJson.path("response").path("merchant_uid").asText();

            PaymentStatus successStatus = PaymentStatus.PAYCOMPLETE;
//            KafkaPayment(pasMerchant_uid, successStatus);

            Card CardEntity = new Card(KafkaMerchant_uid, KafkaAmount, KafkaCard_number, KafkaExpiry, KafkaBirth,
                    KafkaPwd_2digit, KafkaCvc, LocalDateTime.now());
            cardRepository.save(CardEntity);

            PaymentRequest payment = PaymentRequest.builder()
                    .order_id(KafkaMerchant_uid)
                    .status(String.valueOf(successStatus))
                    .build();

            String message = payment.getOrder_id() + "번 결제됨.";
            eventPublisher.publishEvent(new PaymentEvent(payment, message));


        } else {
            PaymentStatus failureStatus = PaymentStatus.PAYFAIL;
//            KafkaPayment(KafkaMerchant_uid, failureStatus);
        }
        return null;
    }

//    public void KafkaPayment(String Pay_merchant_uid, PaymentStatus paymentStatus) {
//        StatusUpdate statusupdate = new StatusUpdate();
//        statusupdate.orderId = Pay_merchant_uid;
//        statusupdate.paymentStatus = paymentStatus;
//
//        try {
//            String statusUpdateJson = objectMapper.writeValueAsString(statusupdate);
//            kafkaTemplate.send("paymentStatus-topic", statusUpdateJson);
//        } catch (Exception e) {
//            System.err.println("KafkaPaymentException");
//        }
//    }
}


