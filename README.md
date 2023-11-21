### jar_Build

./gradlew clean bootjar

### Docker_Build

- docker build --build-arg JAR_FILE=build/libs/GopangPayment-0.0.1-SNAPSHOT.jar -t khyun28/paymentservice:0.0.1 .


---
### docker-compose 실행

- docker-compose up

---

### Kafka_Topic

- 카프카로 받고 보내는 형식은 모두 JSON <br><br>

  - Payment_start  : payment
  - Payment_Detail : payment_detail
  - Payment_Cancel : payment_cancel
  
---
---# PaymentServer1
