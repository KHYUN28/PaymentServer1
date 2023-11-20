package com.example.democonsumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class KafkaConsumerExample {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Kafka 브로커의 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my_consumer_group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        String topic = "paymentStatus-topic";
        consumer.subscribe(Collections.singletonList(topic));

        CompletableFuture<Void> consumerFuture = CompletableFuture.runAsync(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100); // 메시지 폴링 주기 (밀리초)
                records.forEach(record -> {
                    System.out.println(record.value());
                });
            }
        });

        // 어떤 조건 아래에서 CompletableFuture를 완료하거나 취소할 수 있습니다.
        // 예를 들어, 원하는 메시지 개수나 시간 제한 등의 조건을 추가할 수 있습니다.
        try {
            consumerFuture.get(); // CompletableFuture를 완료하거나 예외가 발생할 때까지 대기
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // 프로그램 종료 또는 필요한 시점에 CompletableFuture를 취소할 수 있습니다.
        // consumerFuture.cancel(true);

        // 컨슈머를 종료합니다.
        consumer.close();
    }
}
