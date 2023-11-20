package com.example.demopayment.Service;

import com.example.demopayment.authentication.TokenService;
import com.example.demopayment.dto.OrderRequest;
import com.example.demopayment.dto.Paymentdto.PaymentStatus;
import com.example.demopayment.event.PaymentEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class Payment_Start {

    @Value("${portone.apiBaseUrl}")
    private String API_BASE_URL;
    @Value("${portone.paymentstart}")
    private String PAYMENT_ENDPOINT;

    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public Consumer<String> consumerBinding() {
        return jsonString -> { try {Payment(jsonString);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e); }
        };
    }

    @Transactional
    public PaymentEvent Payment(String jsonString) throws IOException, JSONException{

        TokenService tokenService = new TokenService();
        String accessToken = tokenService.getToken();

        RestTemplate restTemplate = new RestTemplate();
        JSONObject kafkajsonBody = new JSONObject(jsonString);

        System.out.println("KafkaPaymentReceived : " + jsonString);

        String pay_merchant_uid = kafkajsonBody.optString("order_id", null);
        int pay_amount = kafkajsonBody.optInt("amount", 0);

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
        jsonBody.put("merchant_uid", pay_merchant_uid);
        jsonBody.put("amount", pay_amount);
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
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_BASE_URL + PAYMENT_ENDPOINT, requestEntity, String.class);

//        int responseCode = responseEntity.getStatusCodeValue(); // HTTP 응답 코드 얻기
//        System.out.println("Response: " + responseEntity.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(responseEntity.getBody());

        String padPaymentStatus = responseJson.path("response").path("status").asText();

        if (Objects.equals(padPaymentStatus, "paid")) {
            PaymentStatus successStatus = PaymentStatus.PAYCOMPLETE;

            OrderRequest payment = OrderRequest.builder()
                    .order_id(pay_merchant_uid)
                    .StatusUpdate(successStatus)
                    .build();

            // 로깅을 위한 메시지 생성 및 이벤트 발행
            String message = payment.getOrder_id() + "번 결제성공";

            // 로깅을 위한 메시지 생성 및 이벤트 발행
            eventPublisher.publishEvent(new PaymentEvent(payment, message));
        } else {
            // 파싱할 데이터 추출
            PaymentStatus failureStatus = PaymentStatus.PAYFAIL;

            OrderRequest payment = OrderRequest.builder()
                    .order_id(pay_merchant_uid)
                    .StatusUpdate(failureStatus)
                    .build();

            String message = payment.getOrder_id() + "번 결제실패";

            eventPublisher.publishEvent(new PaymentEvent(payment, message));
        }
        return null;
    }
}