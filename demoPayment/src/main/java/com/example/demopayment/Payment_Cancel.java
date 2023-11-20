package com.example.demopayment;

import com.example.demopayment.authentication.TokenService;
import com.example.demopayment.dto.Canceldto.CancelStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Payment_Cancel {

    @Value("${portone.apiBaseUrl}")
    private String API_BASE_URL;
    @Value("${portone.paymentCancel}")
    private String PAYMENT_CANCEL_ENDPOINT;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "paymentCancelRequest-topic", groupId = "payment")
    public String PaymentCancel(String jsonString) throws org.json.JSONException {

        JSONObject jsonBody = new JSONObject(jsonString);
        System.out.println(" KafkaCancelReceived: " + jsonString);

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
                System.out.println("상태: " + status);

                int cancelAmount = jsonObject.get("response").getAsJsonObject().get("cancel_amount").getAsInt();
                int       amount = jsonObject.get("response").getAsJsonObject().get("amount").getAsInt();
//                System.out.println("==========================");
//                System.out.println("취소 금액: " + cancelAmount);
//                System.out.println("금액: " + amount);

                if (cancelAmount > amount) {
                    System.out.println("환불 가능한 금액보다 높은 금액을 입력했습니다.");
                } else {
                    int remainingBalance = amount - cancelAmount;
//                    System.out.println("남은 잔액: " + remainingBalance);
//                    System.out.println("==========================");

                    KafkaPaymentCancel(kafkaMerchant_uid, cancelAmount,amount, remainingBalance, status);

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("cancelAmount", cancelAmount);
                    jsonResponse.put("amount", amount);
                    jsonResponse.put("remainingBalance", remainingBalance);

                    return jsonResponse.toString();
                }
            } else {
                System.err.println("HTTP 요청이 상태 코드 " + statusCode + "로 실패했습니다.");
            }
        } catch (Exception e) {
            System.err.println("환불 가능한 금액을 초과했습니다 : Exception e");
        }
        return null;
    }

    public void KafkaPaymentCancel(String merchant_Uid, int cancelAmount, int amount, int remainingBalance, String status){

        CancelStatus cancelStatus = new CancelStatus(merchant_Uid, cancelAmount, amount, remainingBalance, status);

        cancelStatus.merchant_Uid = merchant_Uid;
        cancelStatus.cancelAmount = cancelAmount;
        cancelStatus.amount = amount;
        cancelStatus.remainingBalance = remainingBalance;
        cancelStatus.status = status;
        try {
            String statusUpdateJson = objectMapper.writeValueAsString(cancelStatus);
            kafkaTemplate.send("paymentCancelStatus-topic", statusUpdateJson);
        } catch (Exception e) {
            System.out.println("KafkaCancelException");
        }
    }
}