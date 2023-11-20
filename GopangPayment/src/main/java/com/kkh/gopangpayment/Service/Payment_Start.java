package com.kkh.gopangpayment.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkh.gopangpayment.authentication.TokenService;
import com.kkh.gopangpayment.domain.Card;
import com.kkh.gopangpayment.dto.Paymentdto.PaymentStatus;
import com.kkh.gopangpayment.dto.Paymentdto.StatusUpdate;
import com.kkh.gopangpayment.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
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
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "paymentRequest-topic", groupId = "payment")
    public String Payment(String jsonString) throws IOException, JSONException{

        TokenService tokenService = new TokenService();
        String accessToken = tokenService.getToken();

        RestTemplate restTemplate = new RestTemplate();
        JSONObject kafkajsonBody = new JSONObject(jsonString);

        System.err.println("KafkaPaymentReceived : " + jsonString);

        // Kafka 메시지에서 관련 정보 추출
        String KafkaMerchant_uid = kafkajsonBody.optString("order_id", null);
        int KafkaAmount = kafkajsonBody.optInt("amount", 0);

        // 임시로 하드코딩된 카드 정보 (실제로는 Kafka 메시지에서 추출해야 함)
        String KafkaCard_number = "5465-9699-1234-5678";
        String KafkaExpiry = "2027-05";
        String KafkaBirth = "940123";
        String KafkaPwd_2digit = "12";
        String KafkaCvc = "123";
        
        // 회원에서 받아와야할 카드정보
//        String KafkaCard_number = kafkajsonBody.optString("card_number", null);
//        String KafkaExpiry = kafkajsonBody.optString("expiry", null);
//        String KafkaBirth = kafkajsonBody.optString("birth", null);
//        String KafkaPwd_2digit = kafkajsonBody.optString("pwd_2digit", null);
//        String KafkaCvc = kafkajsonBody.optString("cvc", null);

        // 결제 시작 요청을 위한 JSON 바디 생성
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("merchant_uid", KafkaMerchant_uid);
        jsonBody.put("amount", KafkaAmount);
        jsonBody.put("card_number", KafkaCard_number);
        jsonBody.put("expiry", KafkaExpiry);
        jsonBody.put("birth", KafkaBirth);
        jsonBody.put("pwd_2digit", KafkaPwd_2digit);
        jsonBody.put("cvc", KafkaCvc);

        // JSON 데이터와 적절한 미디어 타입을 설정하여 HttpEntity 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody.toString(), headers);

        // API에 HTTP POST 요청을 보내고 응답을 받음
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_BASE_URL + PAYMENT_ENDPOINT,
                requestEntity, String.class);

        // 응답에서 JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(responseEntity.getBody());

        // 결제 상태 확인
        String padPaymentStatus = responseJson.path("response").path("status").asText();

        if (Objects.equals(padPaymentStatus, "paid")) { // 결제가 성공한 경우

            // 파싱할 데이터 추출
            String pasMerchant_uid = responseJson.path("response").path("merchant_uid").asText();

            // 결제 성공 상태 업데이트 및 Kafka 이벤트 전송
            PaymentStatus successStatus = PaymentStatus.PAYCOMPLETE;
            KafkaPayment(pasMerchant_uid, successStatus);

            // 결제 정보를 저장할 Card 엔티티 생성 및 저장
            Card CardEntity = new Card(KafkaMerchant_uid, KafkaAmount, KafkaCard_number, KafkaExpiry, KafkaBirth,
                    KafkaPwd_2digit, KafkaCvc, successStatus, LocalDateTime.now());
            cardRepository.save(CardEntity);

        } else { // 결제가 실패한 경우

            // 결제 실패 상태 업데이트 및 Kafka 이벤트 전송
            PaymentStatus failureStatus = PaymentStatus.PAYFAIL;
            KafkaPayment(KafkaMerchant_uid, failureStatus);
        }
        return null;
    }

    // Kafka로 결제 상태를 전송하는 메서드
    public void KafkaPayment(String Pay_merchant_uid, PaymentStatus paymentStatus) {
        StatusUpdate statusupdate = new StatusUpdate();
        statusupdate.orderId = Pay_merchant_uid;
        statusupdate.paymentStatus = paymentStatus;

        try {
            String statusUpdateJson = objectMapper.writeValueAsString(statusupdate);
            kafkaTemplate.send("paymentStatus-topic", statusUpdateJson);
        } catch (Exception e) {
            System.err.println("KafkaPaymentException");
        }
    }
}


