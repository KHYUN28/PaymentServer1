package com.kkh.gopangpayment.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkh.gopangpayment.authentication.TokenService;
import com.kkh.gopangpayment.domain.Cancel;
import com.kkh.gopangpayment.dto.Canceldto.CancelStatus;
import com.kkh.gopangpayment.repository.CancelRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class Payment_Cancel {

    @Value("${portone.apiBaseUrl}")
    private String API_BASE_URL;
    @Value("${portone.paymentCancel}")
    private String PAYMENT_CANCEL_ENDPOINT;

    @Autowired
    private CancelRepository cancelRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "paymentCancelRequest-topic", groupId = "payment")
    public String PaymentCancel(String jsonString) throws org.json.JSONException {

        // Kafka로부터 수신한 JSON 파싱
        JSONObject jsonBody = new JSONObject(jsonString);
        System.err.println(" KafkaCancelReceived: " + jsonString);

        // Kafka 메시지에서 관련 정보 추출
        String kafkaMerchant_uid = jsonBody.getString("order_id");
        int kafkaAmount = jsonBody.getInt("amount");

        try {
            // 토큰 서비스를 이용해 액세스 토큰 획득
            TokenService tokenService = new TokenService();
            String accessToken = tokenService.getToken();

            // RestTemplate 인스턴스 생성
            RestTemplate restTemplate = new RestTemplate();

            // merchant_uid와 amount를 포함한 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant_uid", kafkaMerchant_uid);
            requestBody.put("amount", kafkaAmount);

            // HTTP 요청을 위한 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            // 요청 바디와 헤더를 이용하여 HTTP 요청 엔터티 생성
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

                // 응답에서 관련 정보 추출
                String status = jsonObject.get("response").getAsJsonObject().get("status").getAsString();
                int cancelAmount = jsonObject.get("response").getAsJsonObject().get("cancel_amount").getAsInt();
                int       amount = jsonObject.get("response").getAsJsonObject().get("amount").getAsInt();

                // Cancel 객체 생성 및 cancelRepository에 저장
                Cancel paymentCancel = new Cancel(kafkaMerchant_uid, cancelAmount, kafkaAmount, status);
                cancelRepository.save(paymentCancel);

                if (cancelAmount > amount) {
                    System.err.println("환불 가능한 금액보다 높은 금액을 입력했습니다.");
                } else {
                    // 남은 잔액 계산 및 Kafka 이벤트 트리거
                    int remainingBalance = amount - cancelAmount;
                    KafkaPaymentCancel(kafkaMerchant_uid, cancelAmount,amount, remainingBalance, status);
                }
            } else {
                System.err.println("HTTP 요청이 상태 코드 " + statusCode + "로 실패했습니다.");
            }
        } catch (Exception e) {
            System.err.println("환불 가능한 금액을 초과했습니다 : Exception e");
        }
        return null;
    }

    // Kafka로 결제 취소 상태를 전송하는 메서드
    public void KafkaPaymentCancel(String kafkaMerchant_uid, int cancelAmount, int kafkaAmount, int remainingBalance, String status){

        CancelStatus cancelStatus = new CancelStatus(kafkaMerchant_uid, cancelAmount, kafkaAmount, remainingBalance, status);

        cancelStatus.merchant_Uid = kafkaMerchant_uid;
        cancelStatus.cancelAmount = cancelAmount;
        cancelStatus.amount = kafkaAmount;
        cancelStatus.remainingBalance = remainingBalance;
        cancelStatus.status = status;

        try {
            // 객체를 JSON으로 변환하고 Kafka로 전송
            String statusUpdateJson = objectMapper.writeValueAsString(cancelStatus);
            kafkaTemplate.send("paymentCancelStatus-topic", statusUpdateJson);
            System.err.println("ToOrder = " + statusUpdateJson);
        } catch (Exception e) {
            System.err.println("KafkaCancelException");
        }
    }
}