package com.example.demopayment.Service;

import com.example.demopayment.authentication.TokenService;
import com.example.demopayment.dto.Canceldto.CancelStatus;
import com.example.demopayment.event.PaymentCancelEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class Payment_Cancel {

    @Value("${portone.apiBaseUrl}")
    private String API_BASE_URL;
    @Value("${portone.paymentCancel}")
    private String PAYMENT_CANCEL_ENDPOINT;

    private final ApplicationEventPublisher eventPublisher;

    public Payment_Cancel(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    @Bean
    public Consumer<String> cancelBinding() {
        return jsonString -> {

            String result = null;
            try {
                result = String.valueOf(PaymentCancel(jsonString));
            } catch (org.json.JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    public String PaymentCancel(String jsonString) throws org.json.JSONException {

        JSONObject jsonBody = new JSONObject(jsonString);
        System.out.println("KafkaCancelReceived: " + jsonString);

        String kafkaMerchant_uid = jsonBody.getString("order_id");
        int kafkaAmount = jsonBody.getInt("amount");

        try {
            TokenService tokenService = new TokenService();
            String accessToken = tokenService.getToken();

            // RestTemplate 인스턴스 생성
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant_uid", kafkaMerchant_uid);
            requestBody.put("amount", kafkaAmount);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            // 요청 바디와 헤더를 이용하여 요청 엔터티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // API의 URL 정의
            String apiUrl = API_BASE_URL + PAYMENT_CANCEL_ENDPOINT;

            // HTTP POST 요청을 보내고 응답을 받음
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

            // HTTP 상태 코드 확인
            HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();

                // JSON 응답 파싱
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(responseBody).getAsJsonObject();

                String status = jsonObject.get("response").getAsJsonObject().get("status").getAsString();
//                System.out.println("상태: " + status);

                int cancelAmount = jsonObject.get("response").getAsJsonObject().get("cancel_amount").getAsInt();
                int       amount = jsonObject.get("response").getAsJsonObject().get("amount").getAsInt();


                if (cancelAmount > amount) {
                    System.out.println("환불 가능한 금액보다 높은 금액을 입력했습니다.");
                } else {
                    int remainingBalance = amount - cancelAmount;

//======================================================================
                    CancelStatus paymentCancel = CancelStatus.builder()
                            .orderId(Long.valueOf(kafkaMerchant_uid))
                            .cancelAmount(cancelAmount)
                            .amount(amount)
                            .remainingBalance(remainingBalance)
                            .status(status)
                            .build();

                    // 로깅을 위한 메시지 생성 및 이벤트 발행
                    String message = paymentCancel.getOrderId() + "번 취소됨.";

//                     로깅을 위한 메시지 생성 및 이벤트 발행
                    eventPublisher.publishEvent(new PaymentCancelEvent(paymentCancel, message));
//======================================================================
                }
            } else {
                System.err.println("HTTP 요청이 상태 코드 " + statusCode + "로 실패했습니다.");
            }
        } catch (Exception e) {
            System.err.println("환불 가능한 금액을 초과했습니다 : Exception e");
        }
        return null;
    }

}